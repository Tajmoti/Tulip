package ui.shared

import react.Props
import react.dom.span
import react.fc

external interface BadgeProps : Props {
    var message: String
    var color: BadgeType
}

enum class BadgeType {
    Success,
    Danger
}

val PillBadge = fc<BadgeProps> { props ->
    span("badge badge-pill badge-${props.color.name.lowercase()} ml-1") { +props.message }
}