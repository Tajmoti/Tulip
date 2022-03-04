package ui.search

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtulip.ui.search.SearchUi
import react.Props
import react.dom.onClick
import react.dom.span
import react.fc
import ui.listButton
import ui.renderLanguageBadge

internal external interface SearchResultProps : Props {
    var group: GroupedSearchResult
    var onResultClicked: (ItemKey) -> Unit
}

internal val SearchResult = fc<SearchResultProps> { (group, onResultClicked) ->
    listButton {
        attrs.onClick = { _ ->
            val key = group.results.firstOrNull()?.tmdbId!!
            onResultClicked(key)
        }
        TypeBadge { attrs.group = group }
        val info = SearchUi.getItemInfoForDisplay(group)
        +info.name
        group.results.firstNotNullOfOrNull { it.info.firstAirDateYear }?.let {
            YearBadge { attrs.year = it }
        }
        span("ml-2") {
            for (language in SearchUi.getLanguagesForItem(group)) {
                renderLanguageBadge(language)
            }
        }
    }
}


private operator fun SearchResultProps.component1() = group
private operator fun SearchResultProps.component2() = onResultClicked
