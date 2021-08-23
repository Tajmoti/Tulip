package com.tajmoti.tulip.ui.season

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtvprovider.Season
import com.tajmoti.tulip.R
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val tvDataService: TvDataService
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)

    /**
     * State of this ViewModel represented as values of [State]
     */
    val state: LiveData<State> = _state

    /**
     * Text to be shown in case of error or null
     */
    val statusText = Transformations.map(state) {
        when (it) {
            is State.Error -> R.string.something_went_wrong
            else -> null
        }
    }


    fun fetchEpisodes(service: StreamingService, tvShowId: String, seasonId: String) {
        val key = SeasonKey(service, tvShowId, seasonId)
        performStatefulOneshotOperation(_state, State.Loading, State.Idle) {
            fetchEpisodesToState(key)
        }
    }

    private suspend fun fetchEpisodesToState(key: SeasonKey): State {
        val seasons = tvDataService.getSeason(key)
            .getOrElse { return State.Error(it.message ?: it.javaClass.name) }
        return State.Success(seasons)
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