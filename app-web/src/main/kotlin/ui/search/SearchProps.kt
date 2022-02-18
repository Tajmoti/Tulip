package ui.search

import com.tajmoti.libtulip.model.key.ItemKey
import react.Props

external interface SearchProps : Props {
    var query: String
    var onResultClicked: (ItemKey) -> Unit
}
