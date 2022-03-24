package ui.tvshow

import com.tajmoti.libtulip.model.info.Season
import react.Props
import react.dom.option
import react.fc

internal external interface SeasonDropdownItemProps : Props {
    var season: Season
}

internal val SeasonDropdownItem = fc<SeasonDropdownItemProps> { props ->
    option {
        +"Season ${props.season.seasonNumber}"
        attrs.value = props.season.seasonNumber.toString()
    }
}
