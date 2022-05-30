package ui.shared

import react.Props
import react.dom.attrs
import react.dom.div
import react.fc

internal external interface HorizontalProgressBarProps : Props {
    var title: String
}

internal val HorizontalProgressBar = fc<HorizontalProgressBarProps> { (title) ->
    div("my-3 text-center") {
        div("progress") {
            div("progress-bar progress-bar-striped progress-bar-animated progress-bar-done") {
                attrs {
                    attributes["role"] = "progressbar"
                }
            }
        }
        +title
    }
}

private operator fun HorizontalProgressBarProps.component1() = title