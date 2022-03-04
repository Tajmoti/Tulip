package ui.library

import com.tajmoti.libtulip.ui.library.LibraryItem
import react.Props
import react.dom.div
import react.dom.img
import react.fc
import react.router.dom.Link
import ui.getUrlForItem

internal val LibraryItem = fc<LibraryItemProps> { props ->
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

external interface LibraryItemProps : Props {
    var item: LibraryItem
}