package ui.search

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.dto.SearchResultDto
import react.Props
import react.dom.div
import react.fc

internal external interface SearchResultListProps : Props {
    var groups: List<SearchResultDto>
    var onResultClicked: (ItemKey) -> Unit
}

internal val SearchResultList = fc<SearchResultListProps> { (groups, onResultClicked) ->
    div("list-group") {
        for (group in groups) {
            SearchResult {
                attrs.group = group
                attrs.onResultClicked = onResultClicked
            }
        }
    }
}


private operator fun SearchResultListProps.component1() = groups
private operator fun SearchResultListProps.component2() = onResultClicked