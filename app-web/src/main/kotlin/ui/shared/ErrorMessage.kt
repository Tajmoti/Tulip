package ui.shared

import react.Props
import react.dom.h4
import react.fc

external interface ErrorMessageProps : Props {
    var message: String
}

val ErrorMessage = fc<ErrorMessageProps> { props ->
    h4("text-center mt-5") { +props.message }
}
