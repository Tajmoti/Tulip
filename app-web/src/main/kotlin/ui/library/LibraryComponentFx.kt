package ui.library

import com.tajmoti.libtulip.ui.library.LibraryItem
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import react.Props
import react.dom.div
import react.dom.img
import react.fc
import react.router.dom.Link
import ui.EmptyView
import ui.getUrlForItem
import ui.react.useViewModel

val Library = fc<Props> {
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

external interface LibraryItemProps : Props {
    var item: LibraryItem
}

val LibraryItem = fc<LibraryItemProps> { props ->
    val item = props.item
    div("m-2") {
        Link {
            attrs.to = getUrlForItem(item.key)
            img(src = item.posterPath ?: "") {
                attrs.width = "150em"
                attrs.height = "225em"
            }
        }
    }
}