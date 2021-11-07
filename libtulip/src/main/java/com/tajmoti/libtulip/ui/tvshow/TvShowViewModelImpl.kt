package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class TvShowViewModelImpl constructor(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: FavoritesRepository,
    private val viewModelScope: CoroutineScope,
    private val initialItemKey: TvShowKey,
) : TvShowViewModel {
    private val itemKey = MutableSharedFlow<TvShowKey>(1)
    private val stateWithName = itemKey
        .flatMapLatest { fetchSeasonsToState(it) }
    val state = stateWithName
        .onEach { logger.warn("State is $it") }
        .map { it.state }
        .stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)
    override val name = stateWithName
        .map { it.name }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    override val backdropPath = state
        .map(viewModelScope) { (it as? State.Success)?.backdropPath }
    override val seasons = state
        .map(viewModelScope) { (it as? State.Success)?.seasons?.let { s -> sortSpecialsLast(s) } }
    override val isFavorite = favoritesRepository.isFavorite(initialItemKey)
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    override val error = state
        .map(viewModelScope) { it is State.Error }

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

    private fun fetchSeasonsToState(key: TvShowKey): Flow<StateWithName> {
        return when (key) {
            is TvShowKey.Hosted -> getHostedTvShowAsState(key)
            is TvShowKey.Tmdb -> getTmdbTvShowAsState(key)
        }
    }

    private fun getHostedTvShowAsState(key: TvShowKey.Hosted): Flow<StateWithName> {
        val nameFlow = hostedTvDataRepository.getTvShow(key)
            .map { (it as? NetworkResult.Success)?.data?.name }
        val stateFlow = hostedTvDataRepository.getSeasons(key)
            .map {
                when (it) {
                    is NetworkResult.Success -> State.Success(null, it.data)
                    else -> State.Error
                }
            }
        return combine(nameFlow, stateFlow) { name, state ->
            StateWithName(state, name)
        }
    }

    private fun getTmdbTvShowAsState(key: TvShowKey.Tmdb): Flow<StateWithName> {
        return tmdbRepo.getTvShowWithSeasons(key)
            .map { result ->
                val state = withContext(Dispatchers.Default) { resultToState(result) }
                val name = result.data?.name
                StateWithName(state, name)
            }
    }

    private fun resultToState(result: NetworkResult<out TulipTvShowInfo.Tmdb>): State {
        return when (result) {
            is NetworkResult.Success<out TulipTvShowInfo.Tmdb> ->
                tvToStateFlow(result.data)
            is NetworkResult.Error ->
                State.Error
            is NetworkResult.Cached ->
                tvToStateFlow(result.data) // TODO
        }
    }

    private fun tvToStateFlow(show: TulipTvShowInfo.Tmdb): State {
        val backdropUrl = "https://image.tmdb.org/t/p/original" + show.backdropPath
        return State.Success(backdropUrl, show.seasons)
    }

    private fun sortSpecialsLast(seasons: List<TulipSeasonInfo>) =
        seasons.sortedWith { a, b ->
            val ax = a.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            val bx = b.seasonNumber.takeUnless { it == 0 } ?: Int.MAX_VALUE
            ax.compareTo(bx)
        }

    data class StateWithName(val state: State, val name: String?)

    sealed interface State {
        object Loading : State

        data class Success(
            val backdropPath: String?,
            val seasons: List<TulipSeasonInfo>,
        ) : State

        object Error : State

        val success: Boolean
            get() = this is Success
    }
}