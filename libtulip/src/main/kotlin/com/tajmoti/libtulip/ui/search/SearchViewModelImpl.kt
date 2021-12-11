package com.tajmoti.libtulip.ui.search

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtulip.ui.doCancelableJob
import com.tajmoti.libtulip.ui.search.SearchViewModel.Companion.DEBOUNCE_INTERVAL_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModelImpl(
    private val mappingSearchService: MappingSearchService,
    private val viewModelScope: CoroutineScope,
) : SearchViewModel {
    private val state = MutableStateFlow<State>(State.Idle)
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
        val flowOfStates = mappingSearchService.searchAndCreateMappings(query)
            .map { result -> result.fold({ State.Success(groupAndSortMappedResults(it)) }, { State.Error }) }
        emitAll(flowOfStates)
    }

    private fun groupAndSortMappedResults(it: List<MappedSearchResult>): List<GroupedSearchResult> {
        return groupSearchResults(it).sortedWith(groupComparator)
    }

    private fun groupSearchResults(it: List<MappedSearchResult>): List<GroupedSearchResult> {
        val tvShows = it.mapNotNull { it as? MappedSearchResult.TvShow }
        val movies = it.mapNotNull { it as? MappedSearchResult.Movie }
        return groupItemsByTmdbIds(tvShows) + groupItemsByTmdbIdsMovie(movies)
    }

    private val groupComparator = Comparator<GroupedSearchResult> { a, b ->
        val typeA = getItemType(a)
        val typeB = getItemType(b)
        typeA.compareTo(typeB)
    }

    private fun getItemType(a: GroupedSearchResult?) = when (a) {
        is GroupedSearchResult.TvShow, is GroupedSearchResult.Movie -> 0
        else -> 1
    }

    private fun groupItemsByTmdbIds(items: List<MappedSearchResult.TvShow>): List<GroupedSearchResult> {
        return items
            .groupBy { it.tmdbId }
            .mapNotNull { (key, value) -> key?.let { GroupedSearchResult.TvShow(key, value) } ?: GroupedSearchResult.UnrecognizedTvShow(value) }
    }

    private fun groupItemsByTmdbIdsMovie(items: List<MappedSearchResult.Movie>): List<GroupedSearchResult> {
        return items
            .groupBy { it.tmdbId }
            .mapNotNull { (key, value) -> key?.let { GroupedSearchResult.Movie(key, value) } ?: GroupedSearchResult.UnrecognizedMovie(value) }
    }

    sealed interface State {
        object Idle : State
        object Searching : State
        data class Success(val results: List<GroupedSearchResult>) : State
        object Error : State
    }
}