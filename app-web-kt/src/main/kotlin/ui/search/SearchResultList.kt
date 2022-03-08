package ui.search

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import react.Props
import react.dom.div
import react.fc

internal external interface SearchResultListProps : Props {
    var groups: List<GroupedSearchResult>
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