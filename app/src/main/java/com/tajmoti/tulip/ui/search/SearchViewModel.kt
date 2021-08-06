package com.tajmoti.tulip.ui.search

import androidx.lifecycle.*
import androidx.room.withTransaction
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.model.DbMovie
import com.tajmoti.tulip.model.DbTvShow
import com.tajmoti.tulip.model.StreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val db: AppDatabase
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state
    val idle = Transformations.map(state) { it is State.Idle }
    val loading = Transformations.map(state) { it is State.Searching }
    val noResults = Transformations.map(state) { it is State.Success && it.items.isEmpty() }


    /**
     * Search for a TV show or a movie.
     */
    fun search(query: String) {
        if (_state.value is State.Searching)
            return
        _state.value = State.Searching
        viewModelScope.launch { startFetchEpisodesAsync(query) }
    }

    private suspend fun startFetchEpisodesAsync(query: String) {
        try {
            val searchResult = tvProvider.search(query)
            for ((service, result) in searchResult) {
                val successfulResult = result.getOrNull() ?: continue
                insertToDb(service, successfulResult)
            }
            val successfulItems = searchResult
                .mapNotNull {
                    val res = it.second.getOrNull() ?: return@mapNotNull null
                    it.first to res
                }
                .flatMap { a -> a.second.map { a.first to it } }
            _state.value = State.Success(successfulItems)
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    private suspend fun insertToDb(streamingService: StreamingService, result: List<TvItem>) {
        db.withTransaction {
            for (item in result) {
                if (item is TvItem.Show) {
                    val dbItem = DbTvShow(streamingService, item.key, item.name)
                    db.tvShowDao().insert(dbItem)
                } else {
                    val dbItem = DbMovie(streamingService, item.key, item.name)
                    db.movieDao().insert(dbItem)
                }
            }
        }
    }

    sealed class State {
        object Idle : State()
        object Searching : State()
        data class Success(val items: List<Pair<StreamingService, TvItem>>) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }
}