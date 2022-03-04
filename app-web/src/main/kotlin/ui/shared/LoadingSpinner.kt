package ui.shared

import kotlinx.html.role
import react.Props
import react.dom.div
import react.dom.span
import react.fc

external interface LoadingSpinnerProps : Props {
    var color: SpinnerColor
}

enum class SpinnerColor {
    Default,
    Primary,
    Info
}

val LoadingSpinner = fc<LoadingSpinnerProps> { props ->
    val colorClasses = when (props.color) {
        SpinnerColor.Default -> ""
        SpinnerColor.Primary -> "text-primary"
        SpinnerColor.Info -> "text-info"
    }
    div("d-flex justify-content-center mt-5 mb-5 $colorClasses") {
        div("spinner-border") {
            attrs.role = "status"
            span("sr-only") { +"Loading..." }
        }
    }
}
