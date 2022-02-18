package ui.search

import com.tajmoti.libtulip.model.search.GroupedSearchResult
import react.State

external interface SearchState : State {
    var results: List<GroupedSearchResult>?
}
