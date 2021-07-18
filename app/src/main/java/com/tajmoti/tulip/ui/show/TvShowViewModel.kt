package com.tajmoti.tulip.ui.show

import androidx.lifecycle.*
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.show.Season
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

typealias TvShowWithEpisodes = Pair<TvItem.Show, List<Season>>

@HiltViewModel
class TvShowViewModel @Inject constructor(
    private val tvProvider: TvProvider
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    /**
     * Search for a TV show or a movie.
     */
    fun fetchEpisodes(showId: Serializable) {
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            startFetchEpisodesAsync(showId)
        }
    }

    private suspend fun startFetchEpisodesAsync(showId: Serializable) {
        try {
            val showResult = tvProvider.getShow(showId)
            showResult.onFailure {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            val show = showResult.getOrThrow()
            val result = show.fetchSeasons()
            result.onSuccess { _state.value = State.Success(show to it) }
            result.onFailure { _state.value = State.Error(it.message ?: it.javaClass.name) }
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val items: TvShowWithEpisodes) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }
}