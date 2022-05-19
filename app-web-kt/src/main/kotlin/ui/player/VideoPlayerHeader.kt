package ui.player

import com.tajmoti.libtulip.dto.StreamableInfoDto
import com.tajmoti.libtulip.ui.player.VideoPlayerUtils
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import react.Props
import react.dom.div
import react.dom.span
import react.fc

internal external interface VideoPlayerHeaderProps : Props {
    var name: StreamableInfoDto
    var data: VideoPlayerViewModel.TvShowData?
}

internal val VideoPlayerHeader = fc<VideoPlayerHeaderProps> { (info, data) ->
    div("d-flex flex-row justify-content-between align-items-center py-2") {
        renderEpisodeButton(data?.previousEpisode, "⏮ Previous episode")
//        EpisodeButton { attrs.label = "⏮ Previous episode"; attrs.key = data?.previousEpisode  }
        span("text-center") { +VideoPlayerUtils.streamableToDisplayName(info) }
        renderEpisodeButton(data?.nextEpisode, "Next episode ⏭")
//        EpisodeButton { attrs.label = "Next episode ⏭"; attrs.key = data?.nextEpisode  }
    }
}


private operator fun VideoPlayerHeaderProps.component1() = name
private operator fun VideoPlayerHeaderProps.component2() = data
