package ui

import com.tajmoti.libtulip.model.info.LanguageCode
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.SPAN
import kotlinx.html.role
import react.Props
import react.RBuilder
import react.dom.*
import react.fc

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

val ErrorMessage = fc<ErrorMessageProps> { props ->
    h4("text-center mt-5") { +props.message }
}

external interface ErrorMessageProps : Props {
    var message: String
}