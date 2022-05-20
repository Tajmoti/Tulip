package ui.player

import react.Props
import react.dom.attrs
import react.dom.div
import react.fc

internal val LinkLoadingProgressBar = fc<Props> {
    div("mt-2 text-center") {
        div("progress") {
            div("progress-bar progress-bar-striped progress-bar-animated progress-bar-done") {
                attrs {
                    attributes["role"] = "progressbar"
                }
            }
        }
        +"Links are being loaded"
    }
}