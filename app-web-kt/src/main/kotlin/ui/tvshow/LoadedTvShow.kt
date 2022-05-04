package ui.tvshow

import com.tajmoti.libtulip.dto.SeasonDto
import com.tajmoti.libtulip.dto.TvShowSeasonDto
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import react.Props
import react.dom.div
import react.fc

internal external interface TvShowProps : Props {
    var seasons: List<TvShowSeasonDto>
    var currentSeason: SeasonDto
    var lastPlayedEpisode: EpisodeKey?
    var onSeasonSelected: (SeasonKey) -> Unit
    var isFavorite: Boolean?
    var onFavoriteToggled: () -> Unit
}

internal val TvShow = fc<TvShowProps> { props ->
    div("d-flex justify-content-between mb-2") {
        SeasonDropdown {
            attrs.seasons = props.seasons
            attrs.onSelected = props.onSeasonSelected
            attrs.season = props.currentSeason
        }
        FavoriteToggle {
            attrs.isFavorite = props.isFavorite
            attrs.onFavoriteToggled = props.onFavoriteToggled
        }
    }
    Season {
        attrs.season = props.currentSeason
        attrs.lastPlayedEpisode = props.lastPlayedEpisode
    }
}