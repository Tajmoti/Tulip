package ui.shared

import kotlinx.html.SPAN
import react.dom.span
import react.fc

val ActiveListItem = fc<RenderProp<SPAN>> { props ->
    span("list-group-item active") {
        props.contents(this)
    }
}
