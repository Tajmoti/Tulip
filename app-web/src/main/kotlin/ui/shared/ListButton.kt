package ui.shared

import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import react.dom.button
import react.fc

val ListButton = fc<RenderProp<BUTTON>> { props ->
    button(type = ButtonType.button, classes = "list-group-item list-group-item-action") {
        props.contents(this)
    }
}
