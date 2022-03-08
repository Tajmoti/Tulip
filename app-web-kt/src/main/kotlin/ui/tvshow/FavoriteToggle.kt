package ui.tvshow

import kotlinx.html.ButtonType
import react.Props
import react.dom.button
import react.dom.i
import react.dom.onClick
import react.fc

internal external interface FavoriteToggleProps : Props {
    var onFavoriteToggled: () -> Unit
    var isFavorite: Boolean?
}

internal val FavoriteToggle = fc<FavoriteToggleProps> { props ->
    button(type = ButtonType.button, classes = "btn btn-primary ml-2") {
        attrs.onClick = { props.onFavoriteToggled() }
        if (props.isFavorite!!) {
            i("fa-solid fa-star mr-1") {}
            +"Added"
        } else {
            i("fa-regular fa-star mr-1") {}
            +"Add"
        }
    }
}