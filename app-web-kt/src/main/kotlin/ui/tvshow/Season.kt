package ui.tvshow

import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import react.Props
import react.fc

internal external interface EpisodeListProps : Props {
    var season: TulipSeasonInfo
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