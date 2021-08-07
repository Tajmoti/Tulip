package com.tajmoti.tulip.ui.show

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.Season
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.DbEpisode
import com.tajmoti.tulip.model.DbSeason
import com.tajmoti.tulip.model.StreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias TvShowWithEpisodes = Pair<TvItem.Show, List<Season>>

@HiltViewModel
class TvShowViewModel @Inject constructor(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val db: AppDatabase
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    /**
     * Search for a TV show or a movie.
     */
    fun fetchEpisodes(service: StreamingService, key: String) {
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            startFetchEpisodesAsync(service, key)
        }
    }

    private suspend fun startFetchEpisodesAsync(service: StreamingService, key: String) {
        try {
            val dbInfo = db.tvShowDao().getByKey(service, key) ?: TODO()
            val show = tvProvider.getShow(service, dbInfo.apiInfo).getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            val result = show.fetchSeasons().getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            saveSeasonsToDb(result, service, key)
            _state.value = State.Success(show to result)
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    private suspend fun saveSeasonsToDb(
        result: List<Season>,
        service: StreamingService,
        key: String
    ) {
        db.withTransaction {
            for (season in result) {
                db.seasonDao().insert(DbSeason(service, key, season))
                for (episode in season.episodes) {
                    val dbEpisode = DbEpisode(service, key, season.key, episode)
                    db.episodeDao().insert(dbEpisode)
                }
            }
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