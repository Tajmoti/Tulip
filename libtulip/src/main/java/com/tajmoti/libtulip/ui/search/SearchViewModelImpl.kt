package com.tajmoti.libtulip.ui.search

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.hosted.toItemKey
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.ui.doCancelableJob
import com.tajmoti.libtulip.ui.search.SearchViewModel.Companion.DEBOUNCE_INTERVAL_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModelImpl @Inject constructor(
    private val repository: HostedTvDataRepository,
    private val viewModelScope: CoroutineScope
) : SearchViewModel {
    private val state = MutableStateFlow<State>(State.Idle)
    override val itemToOpen = MutableSharedFlow<ItemKey>()
    override val loading = state.map(viewModelScope) { it is State.Searching }
    override val results = state.map(viewModelScope) {
        (it as? State.Success)?.results ?: emptyList()
    }
    override val status = state.map(viewModelScope) {
        when {
            it is State.Idle ->
                SearchViewModel.Icon.READY
            it is State.Success && it.results.isEmpty() ->
                SearchViewModel.Icon.NO_RESULTS
            it is State.Error ->
                SearchViewModel.Icon.ERROR
            else -> null
        }
    }
    override val canTryAgain = state.map(viewModelScope) { it is State.Error }

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
    override fun submitNewText(query: String) {
        val queryNullable = query.takeIf { it.isNotBlank() }
        viewModelScope.launch { searchFlow.emit(queryNullable) }
    }

    /**
     * Submits an already submitted text again
     */
    override fun resubmitText() {
        onNewSearchQuery(searchFlow.value ?: "")
    }

    /**
     * The user has clicked an identified item
     */
    override fun onItemClicked(id: TmdbItemId) {
        viewModelScope.launch {
            itemToOpen.emit(id.toItemKey())
        }
    }

    private fun onNewSearchQuery(it: String?) {
        viewModelScope.doCancelableJob(this::searchJob, null) {
            val result = if (it == null) {
                flowOf(State.Idle)
            } else {
                startSearchAsync(it)
            }
            state.emitAll(result)
        }
    }

    private suspend fun startSearchAsync(query: String) = flow {
        emit(State.Searching)
        repository.search(query)
            .onSuccess { emit(State.Success(it)) }
            .getOrElse { emit(State.Error) }
    }

    sealed interface State {
        object Idle : State
        object Searching : State
        data class Success(val results: List<TulipSearchResult>) : State
        object Error : State

        val success: Boolean
            get() = this is Success
    }
}