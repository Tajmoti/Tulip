package com.tajmoti.tulip.ui.season

import androidx.lifecycle.*
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.StreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val db: AppDatabase
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state
    val stateText: LiveData<String> = Transformations.map(state) { it.toString() }


    fun fetchEpisodes(service: StreamingService, tvShowId: String, seasonId: String) {
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            startFetchEpisodesAsync(service, tvShowId, seasonId)
        }
    }

    private suspend fun startFetchEpisodesAsync(
        service: StreamingService,
        tvShowId: String,
        seasonId: String
    ) {
        try {
            val dbSeason = db.seasonDao().getForShow(service, tvShowId, seasonId)
                ?: TODO()
            val dbEpisodes = db.episodeDao().getForSeason(service, tvShowId, seasonId)
            val epInfoList = dbEpisodes.map { Episode.Info(it.key, it.name) }
            val info = Season.Info(dbSeason.number, epInfoList)
            val season = tvProvider.getSeason(service, seasonId, info).getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
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