package ui.search

import com.tajmoti.libtulip.dto.SearchResultDto
import kotlinx.html.SPAN
import react.Props
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.span
import react.fc

internal external interface TypeBadgeProps : Props {
    var group: SearchResultDto
}

internal val TypeBadge = fc<TypeBadgeProps> { (group) ->
    when (group) {
        is SearchResultDto.Movie ->
            badge { +"Movie" }
        is SearchResultDto.TvShow ->
            badge { +"TV" }
        is SearchResultDto.UnrecognizedMovie ->
            badge { +"Other Movies" }
        is SearchResultDto.UnrecognizedTvShow ->
            badge { +"Other TV" }
    }
}

private fun RBuilder.badge(block: RDOMBuilder<SPAN>.() -> Unit) {
    span("badge badge-secondary mr-2", block)
}


private operator fun TypeBadgeProps.component1() = group