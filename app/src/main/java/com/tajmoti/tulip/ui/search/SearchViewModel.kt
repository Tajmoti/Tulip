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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val db: AppDatabase
) : ViewModel() {
    companion object {
        /**
         * How often a new search query will be started
         */
        private const val DEBOUNCE_INTERVAL_MS = 1000L
    }

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    /**
     * True if nothing was ever searched
     */
    val idle = Transformations.map(state) { it is State.Idle }

    /**
     * True if currently searching a query
     */
    val loading = Transformations.map(state) { it is State.Searching }

    /**
     * True if the search is finished and no results were returned
     */
    val noResults = Transformations.map(state) { it is State.Success && it.items.isEmpty() }

    /**
     * Flow containing (even partial) queries entered in the search view
     */
    private val searchFlow = MutableStateFlow<String?>(null)

    /**
     * The currently active search, will be replaced when a new query is submitted
     */
    private var searchJob: Job? = null

    init {
        searchFlow
            .debounce { if (it.isNullOrBlank()) 0L else DEBOUNCE_INTERVAL_MS }
            .onEach { onNewSearchQuery(it) }
            .launchIn(viewModelScope)
    }

    /**
     * Submit a new query to be searched
     */
    fun submitNewText(query: String) {
        val queryNullable = query.takeIf { it.isNotBlank() }
        viewModelScope.launch { searchFlow.emit(queryNullable) }
    }

    private fun onNewSearchQuery(it: String?) {
        searchJob?.cancel()
        if (it == null) {
            _state.value = State.Idle
        } else {
            _state.value = State.Searching
            searchJob = viewModelScope.launch { startFetchEpisodesAsync(it) }
        }
    }

    private suspend fun startFetchEpisodesAsync(query: String) {
        try {
            val searchResult = tvProvider.search(query)
            for ((service, result) in searchResult) {
                val successfulResult = result.getOrNull() ?: continue
                insertToDb(service, successfulResult)
            }
            if (searchResult.none { it.second.isSuccess }) {
                _state.value = State.Error("All searches have failed!")
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

    private suspend fun insertToDb(service: StreamingService, result: List<TvItem>) {
        db.withTransaction {
            for (item in result) {
                when (item) {
                    is TvItem.Show -> {
                        val dbItem = DbTvShow(service, item)
                        db.tvShowDao().insert(dbItem)
                    }
                    is TvItem.Movie -> {
                        val dbItem = DbMovie(service, item)
                        db.movieDao().insert(dbItem)
                    }
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