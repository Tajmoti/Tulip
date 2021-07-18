package com.tajmoti.tulip.ui.season

import androidx.lifecycle.*
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.show.Season
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val tvProvider: TvProvider
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state
    val stateText: LiveData<String> = Transformations.map(state) { it.toString() }


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

    private suspend fun startFetchEpisodesAsync(seasonId: Serializable) {
        try {
            val seasonResult = tvProvider.getSeason(seasonId)
            seasonResult.onFailure {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            val season = seasonResult.getOrThrow()
            _state.value = State.Success(season)
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val season: Season) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }
}