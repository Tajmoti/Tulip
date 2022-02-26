package ui.show

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.dom.*
import react.fc
import react.router.useNavigate
import ui.getUrlForStreamable
import ui.listButton
import ui.renderLoading
import ui.useViewModel

external interface TvShowProps : Props {
    var tvShowKey: TvShowKey
}

val TvShow = fc<TvShowProps> { props ->
    val (vm, vmState) = useViewModel<TvShowViewModel, TvShowViewModel.State>(props.tvShowKey) ?: return@fc

    vmState.name?.let { name ->
        TvShowDetails {
            attrs.name = name
            attrs.backdropUrl = vmState.backdropPath
        }
    }

    val seasons = vmState.seasons
    if (seasons != null) {
        val currentSeason = getCurrentSeason(seasons, vmState.selectedSeason)
        SeasonSelector {
            attrs.seasons = seasons
            attrs.onSelected = vm::onSeasonSelected
            attrs.season = currentSeason
            attrs.onSeasonClick = vm::onSeasonSelected
        }
    } else {
        renderLoading()
    }
}

private fun getCurrentSeason(seasons: List<TulipSeasonInfo>, preselected: SeasonKey?): TulipSeasonInfo {
    val currentSeasonKey = preselected ?: seasons.first().key
    return seasons.first { it.key == currentSeasonKey }
}


external interface SeasonSelectorProps : Props, SeasonDropdownProps, EpisodeListProps {
    var onSeasonClick: (SeasonKey) -> Unit
}

private val SeasonSelector = fc<SeasonSelectorProps> { props ->
    SeasonDropdown {
        attrs.seasons = props.seasons
        attrs.season = props.season
        attrs.onSelected = props.onSeasonClick
    }
    Season {
        attrs.season = props.season
    }
}


external interface TvShowDetailsProps : Props {
    var name: String
    var backdropUrl: String?
}

private val TvShowDetails = fc<TvShowDetailsProps> { props ->
    h1("mb-4") { +props.name }
}


external interface EpisodeListProps : Props {
    var season: TulipSeasonInfo
}

private val Season = fc<EpisodeListProps> { props ->
    for (episode in props.season.episodes) {
        Episode {
            attrs.episode = episode
        }
    }
}


external interface EpisodeProps : Props {
    var episode: TulipEpisodeInfo
}

private val Episode = fc<EpisodeProps> { props ->
    val nav = useNavigate()
    listButton {
        div("d-flex flex-row") {
            img(src = props.episode.stillPath ?: "", classes = "img-letterbox flex-shrink-0") {
                attrs.width = "160em"
                attrs.height = "90em"
            }
            div("ml-2") {
                h5 { +"${props.episode.episodeNumber}. ${props.episode.name}" }
                span { +(props.episode.overview ?: "Overview unavailable") }
            }
        }
        attrs.onClick = { _ -> nav(getUrlForStreamable(props.episode.key)) }
    }
}


external interface SeasonDropdownProps : Props {
    var seasons: List<TulipSeasonInfo>
    var season: TulipSeasonInfo
    var onSelected: (SeasonKey) -> Unit
}

private val SeasonDropdown = fc<SeasonDropdownProps> { props ->
    select("custom-select mb-2") {
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


external interface SeasonDropdownItemProps : Props {
    var season: TulipSeasonInfo
}

private val SeasonDropdownItem = fc<SeasonDropdownItemProps> { props ->
    option {
        +"Season ${props.season.seasonNumber}"
        attrs.value = props.season.seasonNumber.toString()
    }
}
