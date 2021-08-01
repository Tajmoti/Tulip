package com.tajmoti.tulip.ui.search

import androidx.lifecycle.*
import androidx.room.withTransaction
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.libtvprovider.TvProvider
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
    private val tvProvider: TvProvider,
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
            val result = tvProvider.search(query).getOrElse {
                _state.value = State.Error(it.message ?: it.javaClass.name)
                return
            }
            insertToDb(result)
            _state.value = State.Success(result)
        } catch (e: CancellationException) {
            _state.value = State.Idle
        }
    }

    private suspend fun insertToDb(result: List<TvItem>) {
        db.withTransaction {
            for (item in result) {
                if (item is TvItem.Show) {
                    val dbItem = DbTvShow(StreamingService.PRIMEWIRE, item.key, item.name)
                    db.tvShowDao().insert(dbItem)
                } else {
                    val dbItem = DbMovie(StreamingService.PRIMEWIRE, item.key, item.name)
                    db.movieDao().insert(dbItem)
                }
            }
        }
    }

    sealed class State {
        object Idle : State()
        object Searching : State()
        data class Success(val items: List<TvItem>) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }
}