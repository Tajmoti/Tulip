package ui.search

import com.tajmoti.libtulip.model.search.GroupedSearchResult
import kotlinx.html.SPAN
import react.Props
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.span
import react.fc

internal external interface TypeBadgeProps : Props {
    var group: GroupedSearchResult
}

internal val TypeBadge = fc<TypeBadgeProps> { (group) ->
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

private fun RBuilder.badge(block: RDOMBuilder<SPAN>.() -> Unit) {
    span("badge badge-secondary mr-2", block)
}


private operator fun TypeBadgeProps.component1() = group