package ui.search

import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtulip.ui.search.SearchUi.getItemInfoForDisplay
import com.tajmoti.libtulip.ui.search.SearchUi.getLanguagesForItem
import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import kotlinx.html.SPAN
import react.RBuilder
import react.dom.*
import ui.*

class SearchComponent(props: SearchProps) : ViewModelComponent<SearchProps, SearchViewModel.State, SearchViewModel>(props) {

    override fun getViewModel(): SearchViewModel {
        return SearchViewModelImpl(di.get(), scope)
    }

    override fun componentDidUpdate(prevProps: SearchProps, prevState: ViewModelState<SearchViewModel.State>, snapshot: Any) {
        viewModel.submitNewText(props.query)
    }

    override fun RBuilder.render() {
        if (vmState.loading) {
            renderLoading()
        } else if (vmState.status == SearchViewModel.Icon.NO_RESULTS) {
            ErrorMessage { attrs.message = "No results" }
        } else if (vmState.status == SearchViewModel.Icon.ERROR) {
            ErrorMessage { attrs.message = "Shit, error :/" }
        } else {
            renderSearchResults(vmState.results)
        }
    }

    private fun RBuilder.renderSearchResults(results: List<GroupedSearchResult>) {
        div("list-group") {
            for (group in results) {
                renderSearchResult(group)
            }
        }
    }

    private fun RBuilder.badge(block: RDOMBuilder<SPAN>.() -> Unit) {
        span("badge badge-secondary mr-2", block)
    }

    private fun RBuilder.renderSearchResult(group: GroupedSearchResult) {
        listButton {
            attrs {
                onClick = { _ ->
                    val key = group.results.firstOrNull()?.tmdbId!!
                    props.onResultClicked(key)
                }
            }
            renderTypeBadge(group)
            val info = getItemInfoForDisplay(group)
            +info.name
            group.results.firstNotNullOfOrNull { it.info.firstAirDateYear }?.let {
                renderYearBadge(it)
            }
            span("ml-2") {
                for (language in getLanguagesForItem(group)) {
                    renderLanguageBadge(language)
                }
            }
        }
    }

    private fun RBuilder.renderYearBadge(it: Int) {
        span("badge badge-pill badge-primary ml-2") { +"$it" }
    }

    private fun RBuilder.renderTypeBadge(group: GroupedSearchResult) {
        when (group) {
            is GroupedSearchResult.Movie ->
                badge { +"Movie" }
            is GroupedSearchResult.TvShow ->
                badge { +"TV" }
            is GroupedSearchResult.UnrecognizedMovie ->
                badge { +"Other Movies" }
            is GroupedSearchResult.UnrecognizedTvShow ->
                badge { +"Other TV" }
        }
    }
}
