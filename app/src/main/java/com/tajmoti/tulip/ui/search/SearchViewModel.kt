package com.tajmoti.tulip.ui.search

import androidx.lifecycle.*
import com.tajmoti.libtulip.model.hosted.toItemKey
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.service.SearchService
import com.tajmoti.tulip.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val service: SearchService
) : ViewModel() {
    companion object {
        /**
         * How often a new search query will be started
         */
        private const val DEBOUNCE_INTERVAL_MS = 500L
    }

    private val _state = MutableLiveData<State>(State.Idle)
    private val _itemToOpen = MutableSharedFlow<ItemKey?>()
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
            it is State.Success && it.results.isEmpty() -> R.string.no_results
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
            it is State.Success && it.results.isEmpty() -> R.drawable.ic_sad_24
            it is State.Error -> R.drawable.ic_sad_24
            else -> null
        }
    }

    /**
     * True if the retry button should be shown
     */
    val canTryAgain = Transformations.map(state) { it is State.Error }

    /**
     * Contains the item that should be opened
     */
    val itemToOpen: Flow<ItemKey?> = _itemToOpen

    /**
     * Flow containing (even partial) queries entered in the search view
     */
    private val searchFlow = MutableStateFlow<String?>(null)

    /**
     * The currently active search, will be replaced when a new query is submitted
     */
    private var searchJob: Job? = null

    init {
        @OptIn(FlowPreview::class)
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

    /**
     * The user has clicked an identified item
     */
    fun onItemClicked(id: TmdbItemId) {
        viewModelScope.launch {
            _itemToOpen.emit(id.toItemKey())
        }
    }

    private fun onNewSearchQuery(it: String?) {
        searchJob?.cancel()
        if (it == null) {
            _state.value = State.Idle
        } else {
            _state.value = State.Searching
            searchJob = viewModelScope.launch { _state.value = startSearchAsync(it) }
        }
    }

    private suspend fun startSearchAsync(query: String): State {
        val successfulItems = service.search(query)
            .getOrElse { return State.Error }
        return State.Success(successfulItems)
    }

    sealed class State {
        object Idle : State()
        object Searching : State()
        data class Success(val results: List<TulipSearchResult>) : State()
        object Error : State()

        val success: Boolean
            get() = this is Success
    }
}