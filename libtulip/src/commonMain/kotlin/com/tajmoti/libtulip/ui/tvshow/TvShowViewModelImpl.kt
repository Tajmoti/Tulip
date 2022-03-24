package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.commonutils.mapWith
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TvShowViewModelImpl constructor(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: PlayingHistoryRepository,
    override val viewModelScope: CoroutineScope,
    private val initialItemKey: TvShowKey,
) : TvShowViewModel {
    private val itemKey = MutableStateFlow(initialItemKey)
    private val manuallySelectedSeason = MutableSharedFlow<SeasonKey?>(1)
    private val stateImpl = itemKey
        .flatMapLatest(::loadSeasons)
        .stateInOffload(LoadingState.Loading)
    private val nameImpl = itemKey
        .flatMapLatest(::loadName)
    private val lastPlayedEpisode = itemKey
        .flatMapLatest { historyRepository.getLastPlayedPosition(it) }
        .map { (it?.key as? EpisodeKey) }
    private val selectedSeasonImpl = merge(manuallySelectedSeason, lastPlayedEpisode.map { it?.seasonKey })
        .flatMapLatest { it?.let(::getSeasonWithEpisodes) ?: selectInitialSeason() }
        .mapNotNulls { it.data }
        .stateInOffload(null)

    private fun selectInitialSeason(): Flow<NetworkResult<out SeasonWithEpisodes>?> {
        return stateImpl.flatMapLatest { loadingStateToInitialSeason(it) ?: flowOf(null) }
    }

    private fun loadingStateToInitialSeason(it: LoadingState): Flow<NetworkResult<out SeasonWithEpisodes>>? {
        return (it as? LoadingState.Success)
            ?.seasons
            ?.firstOrNull { it.seasonNumber == 1 }
            ?.let { getSeasonWithEpisodes(it.key) }
    }

    private val isFavoriteImpl = favoritesRepository.isFavorite(initialItemKey)
        .stateInOffload(false)

    override fun toggleFavorites() {
        viewModelScope.launch {
            if (isFavorite.value) {
                favoritesRepository.deleteUserFavorite(initialItemKey)
            } else {
                favoritesRepository.addUserFavorite(initialItemKey)
            }
        }
    }

    override fun onSeasonSelected(season: SeasonKey) {
        viewModelScope.launch { manuallySelectedSeason.emit(season) }
    }

    private fun loadSeasons(key: TvShowKey): Flow<LoadingState> {
        return when (key) {
            is TvShowKey.Hosted -> loadHostedSeasons(key)
            is TvShowKey.Tmdb -> loadTmdbSeasons(key)
        }
    }

    private fun loadTmdbSeasons(key: TvShowKey.Tmdb): Flow<LoadingState> {
        return tmdbRepo.getTvShow(key)
            .map { result -> resultToState(result) }
    }

    private fun loadHostedSeasons(key: TvShowKey.Hosted): Flow<LoadingState> {
        return hostedTvDataRepository.getSeasons(key)
            .map(::toState)
    }

    private fun loadName(key: TvShowKey) = when (key) {
        is TvShowKey.Hosted -> hostedTvDataRepository.getTvShow(key)
            .map { (it as? NetworkResult.Success)?.data?.name }
        is TvShowKey.Tmdb -> tmdbRepo.getTvShow(key)
            .map { result -> result.data?.name }
    }

    private fun getSeasonWithEpisodes(it: SeasonKey) = when (it) {
        is SeasonKey.Hosted -> hostedTvDataRepository.getSeasonWithEpisodes(it)
        is SeasonKey.Tmdb -> tmdbRepo.getSeasonWithEpisodes(it)
    }

    private fun toState(result: NetworkResult<List<Season.Hosted>>): LoadingState {
        return when (result) {
            is NetworkResult.Success -> LoadingState.Success(null, result.data)
            else -> LoadingState.Error
        }
    }

    private fun resultToState(result: NetworkResult<out TvShow.Tmdb>): LoadingState {
        return when (result) {
            is NetworkResult.Success<out TvShow.Tmdb> ->
                tvToStateFlow(result.data)
            is NetworkResult.Error ->
                LoadingState.Error
            is NetworkResult.Cached ->
                tvToStateFlow(result.data) // TODO
        }
    }

    private fun tvToStateFlow(show: TvShow.Tmdb): LoadingState {
        return LoadingState.Success(show.backdropUrl, show.seasons)
    }

    private val internalState = combine(
        stateImpl,
        nameImpl,
        selectedSeasonImpl,
        isFavoriteImpl,
        lastPlayedEpisode
    ) { a, b, c, d, e -> InternalState(a, b, c, d, e) }
        .stateInOffload(InternalState())

    sealed interface LoadingState {
        object Loading : LoadingState

        data class Success(
            val backdropPath: String?,
            val seasons: List<Season>,
        ) : LoadingState

        object Error : LoadingState
    }

    data class InternalState(
        val state: LoadingState = LoadingState.Loading,
        /**
         * Name of the TV show.
         */
        val name: String? = null,
        /**
         * Season to display episodes from.
         */
        val selectedSeason: SeasonWithEpisodes? = null,
        /**
         * Whether this item is saved in the user's favorites
         */
        val isFavorite: Boolean = false,
        /**
         * Last played episode from this TV show.
         */
        val lastPlayedEpisode: EpisodeKey? = null,
    )

    override val state = internalState.mapWith(viewModelScope) {
        TvShowViewModel.State(
            name = name,
            backdropPath = (state as? LoadingState.Success)?.backdropPath,
            seasons = (state as? LoadingState.Success)?.seasons?.let { s -> sortSpecialsLast(s) },
            selectedSeason = selectedSeason,
            lastPlayedEpisode = lastPlayedEpisode,
            error = (state is LoadingState.Error),
            isFavorite = isFavorite
        )
    }

    private fun sortSpecialsLast(seasons: List<Season>): List<Season> {
        return seasons.sortedWith { a, b ->
            val ax = a.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            val bx = b.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            ax.compareTo(bx)
        }
    }
}