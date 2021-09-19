package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.map
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.NetworkResult
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

    override val state: MutableStateFlow<TvShowViewModel.State> = MutableStateFlow(
        TvShowViewModel.State.Loading
    )
    override val name = MutableStateFlow<String?>(null)
    override val isFavorite = MutableStateFlow(false)
    override val error = state.map(viewModelScope) { it is TvShowViewModel.State.Error }


    init {
        fetchTvShowData()
        attachFavorites()
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

    private fun attachFavorites() {
        viewModelScope.launch {
            val flow = favoritesRepository.getUserFavoritesAsFlow()
                .map { result -> result.any { it == itemKey } }
            isFavorite.emitAll(flow)
        }
    }

    override fun toggleFavorites(key: TvShowKey) {
        viewModelScope.launch {
            if (isFavorite.value) {
                favoritesRepository.deleteUserFavorite(key)
            } else {
                favoritesRepository.addUserFavorite(key)
            }
        }
    }

    private suspend inline fun fetchSeasonsToState(key: TvShowKey): Flow<TvShowViewModel.State> {
        return when (key) {
            is TvShowKey.Hosted -> getHostedTvShowAsState(key)
            is TvShowKey.Tmdb -> getTmdbTvShowAsState(key)
        }
    }

    private suspend inline fun getHostedTvShowAsState(key: TvShowKey.Hosted) = flow {
        val show = hostedTvDataRepository.getTvShow(key)
            .getOrElse {
                emit(TvShowViewModel.State.Error)
                return@flow
            }
        name.value = show.info.name
        val result = hostedTvDataRepository.getSeasons(key)
            .getOrElse {
                emit(TvShowViewModel.State.Error)
                return@flow
            }
        val mapped = result.map { season -> season.pairSeasonInfoWithKey(key) }
        emit(TvShowViewModel.State.Success(null, mapped))
    }

    private suspend inline fun getTmdbTvShowAsState(key: TvShowKey.Tmdb): Flow<TvShowViewModel.State> {
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
    ): TvShowViewModel.State {
        return when (result) {
            is NetworkResult.Success<out TmdbCompleteTvShow> ->
                tvToStateFlow(key, result.data.tv, result.data.seasons)
            is NetworkResult.Error ->
                TvShowViewModel.State.Error
            is NetworkResult.Cached ->
                tvToStateFlow(key, result.data.tv, result.data.seasons) // TODO
        }
    }

    private fun tvToStateFlow(
        key: TvShowKey.Tmdb,
        show: Tv,
        seasons: List<com.tajmoti.libtmdb.model.tv.Season>
    ): TvShowViewModel.State {
        val tmdbSeasons = seasons.map { it.toTulipSeasonInfo(key) }
        val backdropUrl = "https://image.tmdb.org/t/p/original" + show.backdropPath
        return TvShowViewModel.State.Success(backdropUrl, tmdbSeasons)
    }
}