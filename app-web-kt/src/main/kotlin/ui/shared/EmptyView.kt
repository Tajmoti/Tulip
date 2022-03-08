package ui.shared

import react.Props
import react.dom.div
import react.dom.h5
import react.dom.h6
import react.fc

external interface EmptyViewProps : Props {
    var primaryText: String
    var secondaryText: String
}

val EmptyView = fc<EmptyViewProps> { props ->
    div("text-center mt-5") {
        h5 { +props.primaryText }
        h6 { +props.secondaryText }
    }
}