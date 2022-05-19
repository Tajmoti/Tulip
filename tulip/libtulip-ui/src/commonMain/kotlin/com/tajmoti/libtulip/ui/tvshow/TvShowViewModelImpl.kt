package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.commonutils.mapWith
import com.tajmoti.libtulip.dto.SeasonDto
import com.tajmoti.libtulip.dto.TvShowDto
import com.tajmoti.libtulip.dto.TvShowSeasonDto
import com.tajmoti.libtulip.facade.PlayingProgressFacade
import com.tajmoti.libtulip.facade.TvShowInfoFacade
import com.tajmoti.libtulip.facade.UserFavoriteFacade
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.result.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TvShowViewModelImpl constructor(
    private val tvShowInfoFacade: TvShowInfoFacade,
    private val playingProgressFacade: PlayingProgressFacade,
    private val favoriteFacade: UserFavoriteFacade,
    override val viewModelScope: CoroutineScope,
    private val initialItemKey: TvShowKey,
) : TvShowViewModel {
    private val itemKey = MutableStateFlow(initialItemKey)
    private val manuallySelectedSeason = MutableSharedFlow<SeasonKey?>(1)
    private val stateImpl = itemKey
        .flatMapLatest(::tvShowInfoToResult)
        .stateInOffload(LoadingState.Loading)

    private val lastPlayedEpisode = itemKey
        .flatMapLatest { playingProgressFacade.getPlayingProgressForTvShow(it) }
        .map { it?.item }
    private val selectedSeasonImpl = merge(manuallySelectedSeason, lastPlayedEpisode.map { it?.seasonKey })
        .flatMapLatest { it?.let { tvShowInfoFacade.getSeason(it) } ?: selectInitialSeason() }
        .mapNotNulls { it.data }
        .stateInOffload(null)

    private fun selectInitialSeason(): Flow<NetworkResult<out SeasonDto>?> {
        return stateImpl.flatMapLatest { loadingStateToInitialSeason(it) ?: flowOf(null) }
    }

    private fun loadingStateToInitialSeason(it: LoadingState): Flow<NetworkResult<out SeasonDto>>? {
        return (it as? LoadingState.Success)
            ?.dto
            ?.seasons
            ?.firstOrNull { it.seasonNumber == 1 }
            ?.let { tvShowInfoFacade.getSeason(it.key) }
    }

    override fun toggleFavorites() {
        viewModelScope.launch {
            if (isFavorite.value) {
                favoriteFacade.removeItemFromFavorites(initialItemKey)
            } else {
                favoriteFacade.addItemToFavorites(initialItemKey)
            }
        }
    }

    override fun onSeasonSelected(season: SeasonKey) {
        viewModelScope.launch { manuallySelectedSeason.emit(season) }
    }

    private fun tvShowInfoToResult(it: TvShowKey): Flow<LoadingState> {
        return tvShowInfoFacade.getTvShowInfo(it)
            .map { it.data?.let { tvShowDto -> LoadingState.Success(tvShowDto) } ?: LoadingState.Error }
    }

    private val internalState = combine(stateImpl, selectedSeasonImpl, lastPlayedEpisode) { a, b, c ->
        when (a) {
            LoadingState.Error -> InternalState() // TODO Error handling
            LoadingState.Loading -> InternalState()
            is LoadingState.Success -> InternalState(a, a.dto.name, b, a.dto.isFavorite, c)
        }
    }.stateInOffload(InternalState())

    sealed interface LoadingState {
        object Loading : LoadingState

        data class Success(
            val dto: TvShowDto
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
        val selectedSeason: SeasonDto? = null,
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
            backdropPath = (state as? LoadingState.Success)?.dto?.backdropPath,
            seasons = (state as? LoadingState.Success)?.dto?.seasons?.let { s -> sortSpecialsLast(s) },
            selectedSeason = selectedSeason,
            lastPlayedEpisode = lastPlayedEpisode,
            error = (state is LoadingState.Error),
            isFavorite = isFavorite
        )
    }

    private fun sortSpecialsLast(seasons: List<TvShowSeasonDto>): List<TvShowSeasonDto> {
        return seasons.sortedWith { a, b ->
            val ax = a.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            val bx = b.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            ax.compareTo(bx)
        }
    }
}