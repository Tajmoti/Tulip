package com.tajmoti.tulip.ui.show

import androidx.lifecycle.*
import com.tajmoti.commonutils.map
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.pairSeasonInfoWithKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.toTulipSeasonInfo
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TvShowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val args = TvShowFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    private val _name = MutableStateFlow<String?>(null)
    private val _isFavorite = MutableStateFlow(false)

    /**
     * Name of the TV show
     */
    val name: StateFlow<String?> = _name

    /**
     * Data loading state
     */
    val state: StateFlow<State> = _state

    /**
     * True if an error occurred during show loading
     */
    val error = state.map(viewModelScope) { it is State.Error }

    /**
     * True if this item is saved in the user's favorites
     */
    val isFavorite: StateFlow<Boolean> = _isFavorite

    init {
        fetchTvShowData()
        attachFavorites()
    }

    /**
     * Retries the last fetching request
     */
    fun retryFetchTvShowData() {
        fetchTvShowData()
    }

    private fun fetchTvShowData() {
        viewModelScope.launch {
            _state.emitAll(fetchSeasonsToState(args.itemKey))
        }
    }

    private fun attachFavorites() {
        viewModelScope.launch {
            val flow = favoritesRepository.getUserFavoritesAsFlow()
                .map { result -> result.any { it == args.itemKey } }
            _isFavorite.emitAll(flow)
        }
    }

    fun toggleFavorites(key: TvShowKey) {
        viewModelScope.launch {
            if (isFavorite.value) {
                favoritesRepository.deleteUserFavorite(key)
            } else {
                favoritesRepository.addUserFavorite(key)
            }
        }
    }

    private suspend inline fun fetchSeasonsToState(key: TvShowKey): Flow<State> {
        return when (key) {
            is TvShowKey.Hosted -> getHostedTvShowAsState(key)
            is TvShowKey.Tmdb -> {
                tmdbRepo.prefetchTvShowData(key)
                getTmdbTvShowAsState(key)
            }
        }
    }

    private suspend inline fun getHostedTvShowAsState(key: TvShowKey.Hosted) = flow {
        val show = hostedTvDataRepository.getTvShow(key)
            .getOrElse {
                emit(State.Error)
                return@flow
            }
        _name.value = show.info.name
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
                _name.value = result.data.tv.name
            }
            .map { result ->
                withContext(Dispatchers.Default) {
                    resultToState(result, key)
                }
            }
    }

    private fun resultToState(
        result: NetworkResult<TmdbCompleteTvShow>,
        key: TvShowKey.Tmdb
    ): State {
        return when (result) {
            is NetworkResult.Success<TmdbCompleteTvShow> ->
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

    sealed class State {
        object Loading : State()

        data class Success(
            val backdropPath: String?,
            val seasons: List<TulipSeasonInfo>
        ) : State()

        object Error : State()

        val success: Boolean
            get() = this is Success
    }
}