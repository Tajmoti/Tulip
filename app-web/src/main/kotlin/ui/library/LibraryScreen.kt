package ui.library

import com.tajmoti.libtulip.ui.library.LibraryViewModel
import react.Props
import react.dom.div
import react.fc
import ui.useViewModel
import ui.shared.EmptyView

val LibraryScreen = fc<Props> {
    val (_, state) = useViewModel<LibraryViewModel, LibraryViewModel.State>() ?: return@fc
    if (state.favoriteItems.isNotEmpty()) {
        div("d-flex flex-wrap justify-content-center justify-content-md-start") {
            for (item in state.favoriteItems) {
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
