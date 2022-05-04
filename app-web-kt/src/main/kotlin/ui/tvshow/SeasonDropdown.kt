package ui.tvshow

import com.tajmoti.libtulip.dto.SeasonDto
import com.tajmoti.libtulip.dto.TvShowSeasonDto
import com.tajmoti.libtulip.model.key.SeasonKey
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.dom.onChange
import react.dom.select
import react.fc

internal external interface SeasonDropdownProps : Props {
    var seasons: List<TvShowSeasonDto>
    var season: SeasonDto
    var onSelected: (SeasonKey) -> Unit
}

internal val SeasonDropdown = fc<SeasonDropdownProps> { props ->
    select("custom-select w-50 w-sm-25") {
        for (season in props.seasons) {
            SeasonDropdownItem { attrs.season = season }
        }
        attrs.value = props.season.seasonNumber.toString()
        attrs.onChange = {
            val seasonNumber = (it.target as HTMLSelectElement).value.toInt()
            val season = props.seasons.first { season -> season.seasonNumber == seasonNumber }
            props.onSelected(season.key)
        }
    }
}
