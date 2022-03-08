package ui.player

import com.tajmoti.libtvprovider.model.VideoStreamRef
import react.Props
import react.dom.iframe
import react.fc

internal external interface IframeVideoPlayerProps : Props {
    var link: VideoStreamRef
}

internal val IframeVideoPlayer = fc<IframeVideoPlayerProps> { (link) ->
    iframe(classes = "w-100") {
        attrs.src = link.url
        attrs.height = "480px"
    }
}


private operator fun IframeVideoPlayerProps.component1() = link
