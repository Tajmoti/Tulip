package com.tajmoti.libtulip.ui.search

import com.tajmoti.commonutils.mapWith
import com.tajmoti.commonutils.onLast
import com.tajmoti.libtulip.dto.SearchResultDto
import com.tajmoti.libtulip.facade.SearchFacade
import com.tajmoti.libtulip.ui.search.SearchViewModel.Companion.DEBOUNCE_INTERVAL_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModelImpl(
    private val mappingSearchService: SearchFacade,
    override val viewModelScope: CoroutineScope,
) : SearchViewModel {
    /**
     * Flow containing (even partial) queries entered into the search bar.
     */
    private val searchQuery = MutableStateFlow<String?>(null)

    /**
     * State of searching of the [searchQuery].
     */
    private val loadingState = searchQuery
        .debounce { if (it.isNullOrBlank()) 0L else DEBOUNCE_INTERVAL_MS }
        .flatMapLatest { it?.let { searchQuery(it) } ?: flowOf(LoadingState.Idle) }
        .stateInOffload(LoadingState.Idle)

    /**
     * Submit a new query to be searched
     */
    override fun submitNewText(query: String) {
        searchQuery.value = query.takeIf { it.isNotBlank() }
    }

    /**
     * Submits an already submitted text again
     */
    override fun resubmitText() {
        val existing = searchQuery.value
        searchQuery.value = null
        searchQuery.value = existing
    }

    private fun searchQuery(query: String) = flow {
        emit(LoadingState.Searching)
        val flowOfStates = mappingSearchService.search(query)
            .map { result ->
                result.fold(
                    { LoadingState.Success(it, false) },
                    { LoadingState.Error }
                )
            }
            .onLast { last ->
                (last as? LoadingState.Success)
                    ?.copy(done = true)
                    ?.let { it -> emit(it) }
            }
        emitAll(flowOfStates)
    }

    sealed interface LoadingState {
        object Idle : LoadingState
        object Searching : LoadingState
        data class Success(val results: List<SearchResultDto>, val done: Boolean) : LoadingState
        object Error : LoadingState
    }


    override val state = loadingState.mapWith(viewModelScope) {
        SearchViewModel.State(
            loading = this is LoadingState.Searching || (this is LoadingState.Success && !this.done),
            results = (this as? LoadingState.Success)?.results ?: emptyList(),
            status = this.toStatusIcon(),
            canTryAgain = this is LoadingState.Error
        )
    }

    private fun LoadingState.toStatusIcon() = when {
        this is LoadingState.Idle -> SearchViewModel.Icon.READY
        this is LoadingState.Success && this.results.isEmpty() -> SearchViewModel.Icon.NO_RESULTS
        this is LoadingState.Error -> SearchViewModel.Icon.ERROR
        else -> null
    }
}