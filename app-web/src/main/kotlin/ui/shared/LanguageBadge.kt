package ui.shared

import com.tajmoti.libtulip.model.info.LanguageCode
import react.Props
import react.dom.span
import react.fc

external interface LanguageBadgeProps : Props {
    var language: LanguageCode
}

val LanguageBadge = fc<LanguageBadgeProps> { props ->
    val flag = languageToFlag(props.language)
    span("ml-2") {
        if (flag != null) {
            +flag
        } else {
            span("badge badge-pill badge-info") { props.language.code.uppercase() }
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