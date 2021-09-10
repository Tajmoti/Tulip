package com.tajmoti.tulip.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tajmoti.commonutils.statefulMap
import com.tajmoti.libtulip.model.hosted.toItemKey
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.tulip.R
import com.tajmoti.tulip.ui.doCancelableJob
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: HostedTvDataRepository
) : ViewModel() {
    companion object {
        /**
         * How often a new search query will be started
         */
        private const val DEBOUNCE_INTERVAL_MS = 500L
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    private val _itemToOpen = MutableSharedFlow<ItemKey>()

    /**
     * True if currently searching a query
     */
    val loading = _state.statefulMap(viewModelScope) { it is State.Searching }

    /**
     * All search results for the entered query
     */
    val results = _state.statefulMap(viewModelScope) {
        when (it) {
            is State.Success -> it.results
            else -> emptyList()
        }
    }

    /**
     * Contains the text that should be shown or null if none
     */
    val statusText = _state.statefulMap(viewModelScope) {
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
    val statusImage = _state.statefulMap(viewModelScope) {
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
    val canTryAgain = _state.statefulMap(viewModelScope) { it is State.Error }

    /**
     * Contains the item that should be opened
     */
    val itemToOpen: Flow<ItemKey> = _itemToOpen

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
        doCancelableJob(this::searchJob, null) {
            val result = if (it == null) {
                flowOf(State.Idle)
            } else {
                startSearchAsync(it)
            }
            _state.emitAll(result)
        }
    }

    private suspend fun startSearchAsync(query: String) = flow {
        emit(State.Searching)
        repository.search(query)
            .onSuccess { emit(State.Success(it)) }
            .getOrElse { emit(State.Error) }
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