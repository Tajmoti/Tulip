package ui.tvshow

import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.EpisodeKey
import react.Props
import react.fc

internal external interface EpisodeListProps : Props {
    var season: SeasonWithEpisodes
    var lastPlayedEpisode: EpisodeKey?
}

internal val Season = fc<EpisodeListProps> { props ->
    for (episode in props.season.episodes) {
        Episode {
            attrs.hasSavedProgress = episode.key == props.lastPlayedEpisode
            attrs.episode = episode
        }
    }
}