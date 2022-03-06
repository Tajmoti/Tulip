package ui.search

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.ui.search.SearchViewModel
import react.Props
import react.fc
import ui.shared.ErrorMessage
import ui.shared.LoadingSpinner
import ui.shared.SpinnerColor
import ui.useViewModel

internal external interface SearchProps : Props {
    var query: String
    var onResultClicked: (ItemKey) -> Unit
}

internal val SearchScreen = fc<SearchProps> { (query, onResultClicked) ->
    val (vm, vmState) = useViewModel<SearchViewModel, SearchViewModel.State>()
    vm.submitNewText(query)

    if (vmState.loading) {
        LoadingSpinner { attrs.color = SpinnerColor.Default }
    } else if (vmState.status == SearchViewModel.Icon.NO_RESULTS) {
        ErrorMessage { attrs.message = "No results" }
    } else if (vmState.status == SearchViewModel.Icon.ERROR) {
        ErrorMessage { attrs.message = "Shit, error :/" }
    } else {
        SearchResultList {
            attrs.groups = vmState.results
            attrs.onResultClicked = onResultClicked
        }
    }
}

private operator fun SearchProps.component1() = query
private operator fun SearchProps.component2() = onResultClicked
