package ui.search

import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtulip.ui.search.SearchUi.getItemInfoForDisplay
import com.tajmoti.libtulip.ui.search.SearchUi.getLanguagesForItem
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import kotlinx.html.SPAN
import react.RBuilder
import react.dom.*
import ui.BaseComponent
import ui.listButton
import ui.renderLanguageBadge
import ui.renderLoading

class SearchComponent(props: SearchProps) : BaseComponent<SearchProps, SearchState>(props) {
    private val viewModel = SearchViewModelImpl(di.get(), scope)

    init {
        state.results = emptyList()
        viewModel.results flowTo { newResults -> updateState { results = newResults } }
    }

    override fun componentDidUpdate(prevProps: SearchProps, prevState: SearchState, snapshot: Any) {
        viewModel.submitNewText(props.query)
    }

    override fun RBuilder.render() {
        val results = state.results
        if (results != null) {
            renderSearchResults(results)
        } else {
            renderLoading()
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
