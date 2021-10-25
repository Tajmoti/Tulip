package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TvShowViewModelImpl constructor(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: FavoritesRepository,
    private val viewModelScope: CoroutineScope,
    private val itemKey: TvShowKey
) : TvShowViewModel {
    private val state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    override val name = MutableStateFlow<String?>(null)
    override val backdropPath = state
        .map(viewModelScope) { (it as? State.Success)?.backdropPath }
    override val seasons = state
        .map(viewModelScope) { (it as? State.Success)?.seasons?.let { s -> sortSpecialsLast(s) } }
    override val isFavorite = favoritesRepository.isFavorite(itemKey)
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    override val error = state
        .map(viewModelScope) { it is State.Error }


    init {
        fetchTvShowData()
    }

    /**
     * Retries the last fetching request
     */
    override fun retryFetchTvShowData() {
        fetchTvShowData()
    }

    private fun fetchTvShowData() {
        viewModelScope.launch {
            state.emitAll(fetchSeasonsToState(itemKey))
        }
    }

    override fun toggleFavorites() {
        viewModelScope.launch {
            if (isFavorite.value) {
                favoritesRepository.deleteUserFavorite(itemKey)
            } else {
                favoritesRepository.addUserFavorite(itemKey)
            }
        }
    }

    private suspend inline fun fetchSeasonsToState(key: TvShowKey): Flow<State> {
        return when (key) {
            is TvShowKey.Hosted -> getHostedTvShowAsState(key)
            is TvShowKey.Tmdb -> getTmdbTvShowAsState(key)
        }
    }

    private suspend inline fun getHostedTvShowAsState(key: TvShowKey.Hosted) = flow {
        val show = hostedTvDataRepository.getTvShow(key)
            .getOrElse {
                emit(State.Error)
                return@flow
            }
        name.value = show.info.name
        hostedTvDataRepository.getSeasons(key)
            .onSuccess { emit(State.Success(null, it)) }
            .onFailure { emit(State.Error) }
    }

    private suspend inline fun getTmdbTvShowAsState(key: TvShowKey.Tmdb): Flow<State> {
        return tmdbRepo.getTvShowWithSeasonsAsFlow(key)
            .onEach { result ->
                if (result !is NetworkResult.Success)
                    return@onEach
                name.value = result.data.name
            }
            .map { result ->
                withContext(Dispatchers.Default) {
                    resultToState(result)
                }
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

    sealed interface State {
        object Loading : State

        data class Success(
            val backdropPath: String?,
            val seasons: List<TulipSeasonInfo>
        ) : State

        object Error : State

        val success: Boolean
            get() = this is Success
    }
}