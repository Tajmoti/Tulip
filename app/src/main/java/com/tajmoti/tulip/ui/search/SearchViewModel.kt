package com.tajmoti.tulip.ui.search

import androidx.lifecycle.*
import androidx.room.withTransaction
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.R
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
        private const val DEBOUNCE_INTERVAL_MS = 500L
    }

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    /**
     * True if currently searching a query
     */
    val loading = Transformations.map(state) { it is State.Searching }

    /**
     * Contains the text that should be shown or null if none
     */
    val statusText = Transformations.map(state) {
        when {
            it is State.Idle -> R.string.search_hint
            it is State.Success && it.items.isEmpty() -> R.string.no_results
            it is State.Error -> R.string.something_went_wrong
            else -> null
        }
    }

    /**
     * A status icon shown above the status text
     */
    val statusImage = Transformations.map(state) {
        when {
            it is State.Idle -> R.drawable.ic_search_24
            it is State.Success && it.items.isEmpty() -> R.drawable.ic_sad_24
            it is State.Error -> R.drawable.ic_sad_24
            else -> null
        }
    }

    /**
     * True if the retry button should be shown
     */
    val canTryAgain = Transformations.map(state) { it is State.Error }

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

    /**
     * Submits an already submitted text again
     */
    fun resubmitText() {
        onNewSearchQuery(searchFlow.value ?: "")
    }

    private fun onNewSearchQuery(it: String?) {
        searchJob?.cancel()
        if (it == null) {
            _state.value = State.Idle
        } else {
            _state.value = State.Searching
            searchJob = viewModelScope.launch { startSearchAsync(it) }
        }
    }

    private suspend fun startSearchAsync(query: String) {
        try {
            val searchResult = tvProvider.search(query)
            for ((service, result) in searchResult) {
                val successfulResult = result.getOrNull() ?: continue
                insertToDb(service, successfulResult)
            }
            if (searchResult.none { it.second.isSuccess }) {
                _state.value = State.Error
                return
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
        object Error : State()

        val success: Boolean
            get() = this is Success
    }
}