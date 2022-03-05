package ui.library

import com.tajmoti.libtulip.ui.library.LibraryViewModel
import react.Props
import react.dom.div
import react.fc
import ui.shared.EmptyView
import ui.shared.LoadingSpinner
import ui.shared.SpinnerColor
import ui.useViewModel

val LibraryScreen = fc<Props> {
    val (_, state) = useViewModel<LibraryViewModel, LibraryViewModel.State>()
    val favorites = state.favoriteItems
    if (favorites == null) {
        LoadingSpinner { attrs.color = SpinnerColor.Default }
    } else if (favorites.isNotEmpty()) {
        div("d-flex flex-wrap justify-content-center justify-content-md-start") {
            for (item in favorites) {
                LibraryItem { attrs.item = item }
            }
        }
    } else {
        EmptyView {
            attrs.primaryText = "Your library is empty."
            attrs.secondaryText = "Search for some items and add them to favorites."
        }
    }
}
