package ui.show

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.TvShowKey
import react.Props

external interface TvShowProps : Props {
    var tvShowKey: TvShowKey
    var onEpisodeClicked: (EpisodeKey) -> Unit
}