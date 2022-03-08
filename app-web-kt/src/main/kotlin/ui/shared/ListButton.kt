package ui.shared

import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import react.dom.button
import react.fc

external interface ListButtonProps : RenderProp<BUTTON> {
    var active: Boolean?
}

val ListButton = fc<ListButtonProps> { props ->
    val extraClasses = if (props.active == true) "active" else ""
    button(type = ButtonType.button, classes = "list-group-item list-group-item-action $extraClasses") {
        props.contents(this)
    }
}
