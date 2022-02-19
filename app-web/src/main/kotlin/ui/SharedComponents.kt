package ui

import com.tajmoti.libtulip.model.info.LanguageCode
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.SPAN
import kotlinx.html.role
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.button
import react.dom.div
import react.dom.span

fun RBuilder.renderLoading(extraClasses: String = "") {
    div("d-flex justify-content-center mt-5 $extraClasses") {
        div("spinner-border") {
            attrs.role = "status"
            span("sr-only") { +"Loading..." }
        }
    }
}

fun RBuilder.listButton(block: RDOMBuilder<BUTTON>.() -> Unit) {
    button(type = ButtonType.button, classes = "list-group-item list-group-item-action", block = block)
}

fun RBuilder.activeListItem(block: RDOMBuilder<SPAN>.() -> Unit) {
    span("list-group-item active", block = block)
}

fun RBuilder.renderLanguageBadge(language: LanguageCode, extraClasses: String = "") {
    span("badge badge-pill badge-info $extraClasses") { +language.code.uppercase() }
}