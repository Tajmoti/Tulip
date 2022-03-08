package ui.search

import react.Props
import react.dom.span
import react.fc

internal external interface YearBadgeProps : Props {
    var year: Int
}

internal val YearBadge = fc<YearBadgeProps> { (year) ->
    span("badge badge-pill badge-primary ml-2") { +"$year" }
}


private operator fun YearBadgeProps.component1() = year
