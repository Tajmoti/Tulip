package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.map
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.pairSeasonInfoWithKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.toTulipSeasonInfo
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
        .map(viewModelScope) { (it as? State.Success)?.seasons }
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
        val result = hostedTvDataRepository.getSeasons(key)
            .getOrElse {
                emit(State.Error)
                return@flow
            }
        val mapped = result.map { season -> season.pairSeasonInfoWithKey(key) }
        emit(State.Success(null, mapped))
    }

    private suspend inline fun getTmdbTvShowAsState(key: TvShowKey.Tmdb): Flow<State> {
        return tmdbRepo.getTvShowWithSeasonsAsFlow(key)
            .onEach { result ->
                if (result !is NetworkResult.Success)
                    return@onEach
                name.value = result.data.tv.name
            }
            .map { result ->
                withContext(Dispatchers.Default) {
                    resultToState(result, key)
                }
            }
    }

    private fun resultToState(
        result: NetworkResult<out TmdbCompleteTvShow>,
        key: TvShowKey.Tmdb
    ): State {
        return when (result) {
            is NetworkResult.Success<out TmdbCompleteTvShow> ->
                tvToStateFlow(key, result.data.tv, result.data.seasons)
            is NetworkResult.Error ->
                State.Error
            is NetworkResult.Cached ->
                tvToStateFlow(key, result.data.tv, result.data.seasons) // TODO
        }
    }

    private fun tvToStateFlow(
        key: TvShowKey.Tmdb,
        show: Tv,
        seasons: List<com.tajmoti.libtmdb.model.tv.Season>
    ): State {
        val tmdbSeasons = seasons.map { it.toTulipSeasonInfo(key) }
        val backdropUrl = "https://image.tmdb.org/t/p/original" + show.backdropPath
        return State.Success(backdropUrl, tmdbSeasons)
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