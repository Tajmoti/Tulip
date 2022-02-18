package ui

import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.role
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.button
import react.dom.div
import react.dom.span

fun RBuilder.renderLoading() {
    div("d-flex justify-content-center mt-5") {
        div("spinner-border") {
            attrs.role = "status"
            span("sr-only") { +"Loading..." }
        }
    }
}

fun RBuilder.listButton(block: RDOMBuilder<BUTTON>.() -> Unit) {
    button(type = ButtonType.button, classes = "list-group-item list-group-item-action", block = block)
}