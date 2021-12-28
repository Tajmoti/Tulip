package com.tajmoti.libtulip.ui.search

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtulip.ui.search.SearchViewModel.Companion.DEBOUNCE_INTERVAL_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModelImpl(
    private val mappingSearchService: MappingSearchService,
    private val viewModelScope: CoroutineScope,
) : SearchViewModel {
    /**
     * Flow containing (even partial) queries entered into the search bar.
     */
    private val searchQuery = MutableStateFlow<String?>(null)

    /**
     * State of searching of the [searchQuery].
     */
    private val state = searchQuery
        .debounce { if (it.isNullOrBlank()) 0L else DEBOUNCE_INTERVAL_MS }
        .flatMapLatest { it?.let { searchQuery(it) } ?: flowOf(State.Idle) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, State.Idle)

    override val loading = state
        .map(viewModelScope) { it is State.Searching }

    override val results = state
        .map(viewModelScope) { (it as? State.Success)?.results ?: emptyList() }

    override val status = state
        .map(viewModelScope) {
            when {
                it is State.Idle -> SearchViewModel.Icon.READY
                it is State.Success && it.results.isEmpty() -> SearchViewModel.Icon.NO_RESULTS
                it is State.Error -> SearchViewModel.Icon.ERROR
                else -> null
            }
        }

    override val canTryAgain = state
        .map(viewModelScope) { it is State.Error }

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