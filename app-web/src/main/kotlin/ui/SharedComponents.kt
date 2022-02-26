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

external interface EmptyViewProps : Props {
    var text: String
}

val EmptyView = fc<EmptyViewProps> { props ->
    div("d-flex justify-content-center mt-5") {
        h5 { +props.text }
    }
}

fun RBuilder.listButton(block: RDOMBuilder<BUTTON>.() -> Unit) {
    button(type = ButtonType.button, classes = "list-group-item list-group-item-action", block = block)
}

fun RBuilder.activeListItem(block: RDOMBuilder<SPAN>.() -> Unit) {
    span("list-group-item active", block = block)
}

fun RBuilder.renderLanguageBadge(language: LanguageCode) {
    val flag = languageToFlag(language)
    span("ml-2") {
        if (flag != null) {
            +flag
        } else {
            span("badge badge-pill badge-info") { language.code.uppercase() }
        }
    }
}

private fun languageToFlag(language: LanguageCode): String? {
    return when (language.code) {
        "en" -> "\uD83C\uDDEC\uD83C\uDDE7"
        "de" -> "\uD83C\uDDE9\uD83C\uDDEA"
        else -> null
    }
}

val PillBadge = fc<BadgeProps> { props ->
    span("badge badge-pill badge-${props.color.name.lowercase()} ml-1") { +props.message }
}

external interface BadgeProps : Props {
    var message: String
    var color: BadgeType
}

enum class BadgeType {
    Success,
    Danger
}

val ErrorMessage = fc<ErrorMessageProps> { props ->
    h4("text-center mt-5") { +props.message }
}

external interface ErrorMessageProps : Props {
    var message: String
}