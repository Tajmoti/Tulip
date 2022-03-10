package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.mapWith
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TvShowViewModelImpl constructor(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: PlayingHistoryRepository,
    override val viewModelScope: CoroutineScope,
    private val initialItemKey: TvShowKey,
) : TvShowViewModel {
    private val itemKey = MutableSharedFlow<TvShowKey>(1)
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
        .stateInOffload(null)
    private val isFavoriteImpl = favoritesRepository.isFavorite(initialItemKey)
        .stateInOffload(false)

    init {
        retryFetchTvShowData()
    }

    /**
     * Retries the last fetching request
     */
    override fun retryFetchTvShowData() {
        viewModelScope.launch {
            itemKey.emit(initialItemKey)
        }
    }

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

    private fun toState(result: NetworkResult<List<TulipSeasonInfo.Hosted>>): LoadingState {
        return when (result) {
            is NetworkResult.Success -> LoadingState.Success(null, result.data)
            else -> LoadingState.Error
        }
    }

    private fun resultToState(result: NetworkResult<out TulipTvShowInfo.Tmdb>): LoadingState {
        return when (result) {
            is NetworkResult.Success<out TulipTvShowInfo.Tmdb> ->
                tvToStateFlow(result.data)
            is NetworkResult.Error ->
                LoadingState.Error
            is NetworkResult.Cached ->
                tvToStateFlow(result.data) // TODO
        }
    }

    private fun tvToStateFlow(show: TulipTvShowInfo.Tmdb): LoadingState {
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
            val seasons: List<TulipSeasonInfo>,
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
        val selectedSeason: SeasonKey? = null,
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

    private fun sortSpecialsLast(seasons: List<TulipSeasonInfo>): List<TulipSeasonInfo> {
        return seasons.sortedWith { a, b ->
            val ax = a.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            val bx = b.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            ax.compareTo(bx)
        }
    }
}