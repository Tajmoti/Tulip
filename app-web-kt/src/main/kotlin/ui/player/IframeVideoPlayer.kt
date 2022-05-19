package ui.player

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import react.Props
import react.dom.iframe
import react.fc

internal external interface IframeVideoPlayerProps : Props {
    var link: StreamingSiteLinkDto
}

internal val IframeVideoPlayer = fc<IframeVideoPlayerProps> { (link) ->
    iframe(classes = "w-100") {
        attrs.src = link.url
        attrs.height = "480px"
    }
}


private operator fun IframeVideoPlayerProps.component1() = link
