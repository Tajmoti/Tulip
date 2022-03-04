package ui.tvshow

import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import react.Props
import react.fc

internal external interface EpisodeListProps : Props {
    var season: TulipSeasonInfo
}

internal val Season = fc<EpisodeListProps> { props ->
    for (episode in props.season.episodes) {
        Episode {
            attrs.episode = episode
        }
    }
}