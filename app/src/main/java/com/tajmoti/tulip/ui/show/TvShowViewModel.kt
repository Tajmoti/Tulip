package com.tajmoti.tulip.ui.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TvShowViewModel @Inject constructor(
    private val hostedTvDataService: HostedTvDataService,
    private val tmdbTvDataService: TvDataService
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    private val _name = MutableLiveData<String?>()

    /**
     * Name of the TV show
     */
    val name: LiveData<String?> = _name

    /**
     * Data loading state
     */
    val state: LiveData<State> = _state

    /**
     * True if an error occurred during show loading
     */
    val error = Transformations.map(state) { it is State.Error }

    /**
     * Retries the last fetching request
     */
    fun retryFetchTvShowData() {
        val lastKey = (state.value as State.Error).lastKey
        fetchTvShowData(lastKey)
    }

    /**
     * Search for a TV show or a movie
     */
    fun fetchTvShowData(key: TvShowKey) {
        performStatefulOneshotOperation(_state, State.Loading, State.Idle) {
            when (key) {
                is TvShowKey.Tmdb -> tmdbTvDataService.prefetchTvShowData(key)
                is TvShowKey.Hosted -> hostedTvDataService.prefetchTvShow(key)
            }
            fetchSeasonsToState(key)
        }
    }

    private suspend fun fetchSeasonsToState(key: TvShowKey): State {
        return when (key) {
            is TvShowKey.Hosted -> getHostedTvShowAsState(key)
            is TvShowKey.Tmdb -> getTmdbTvShowAsState(key)
        }
    }

    private suspend fun getHostedTvShowAsState(key: TvShowKey.Hosted): State {
        val show = hostedTvDataService.getTvShow(key)
            .getOrElse { return State.Error(key) }
        _name.value = show.info.name
        val result = hostedTvDataService.getSeasons(key)
            .getOrElse { return State.Error(key) }
        return State.Success(result.map { SeasonKey.Hosted(key, it.number) })
    }

    private suspend fun getTmdbTvShowAsState(key: TvShowKey.Tmdb): State {
        val show = tmdbTvDataService.getTvShow(key)
            .getOrElse { return State.Error(key) }
        _name.value = show.name
        val tmdbSeasons = show.seasons
            .map { SeasonKey.Tmdb(key, it.seasonNumber) }
        return State.Success(tmdbSeasons)
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val seasons: List<SeasonKey>) : State()
        data class Error(val lastKey: TvShowKey) : State()

        val success: Boolean
            get() = this is Success
    }
}