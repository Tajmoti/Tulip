package com.tajmoti.tulip.ui.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TvShowViewModel @Inject constructor(
    private val tvDataService: TvDataService
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
     * Search for a TV show or a movie
     */
    fun fetchEpisodes(service: StreamingService, key: String) {
        fetchEpisodes(TvShowKey(service, key))
    }

    /**
     * Retries the last fetching request
     */
    fun retryFetchEpisodes() {
        val lastKey = (state.value as State.Error).lastKey
        fetchEpisodes(lastKey)
    }

    private fun fetchEpisodes(key: TvShowKey) {
        performStatefulOneshotOperation(_state, State.Loading, State.Idle) {
            fetchSeasonsToState(key)
        }
    }

    private suspend fun fetchSeasonsToState(key: TvShowKey): State {
        val show = tvDataService.getTvShow(key)
            .getOrElse { return State.Error(key) }
        _name.value = show.name
        val result = tvDataService.fetchAndSaveSeasons(key, show)
            .getOrElse { return State.Error(key) }
        return State.Success(result)
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val seasons: List<Season>) : State()
        data class Error(val lastKey: TvShowKey) : State()

        val success: Boolean
            get() = this is Success
    }
}