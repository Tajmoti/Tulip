package com.tajmoti.tulip.ui.show

import androidx.lifecycle.*
import androidx.room.withTransaction
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
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
     * The last submitted fetch episode request - used for retry
     */
    private var lastKey: TvShowKey? = null

    /**
     * Search for a TV show or a movie
     */
    fun fetchEpisodes(service: StreamingService, key: String) {
        lastKey = TvShowKey(service, key)
        if (_state.value is State.Loading)
            return
        _state.value = State.Loading
        viewModelScope.launch {
            startFetchEpisodesAsync(service, key)
        }
    }

    /**
     * Retries the last fetching request
     */
    fun retryFetchEpisodes() {
        val lastKey = lastKey!!
        fetchEpisodes(lastKey.service, lastKey.key)
    }

    private suspend fun startFetchEpisodesAsync(service: StreamingService, key: String) {
        try {
            val dbInfo = db.tvShowDao().getByKey(service, key) ?: TODO()
            val show = tvProvider.getShow(service, dbInfo.apiInfo).getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            _name.value = show.name
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

    private data class TvShowKey(
        val service: StreamingService,
        val key: String
    )
}