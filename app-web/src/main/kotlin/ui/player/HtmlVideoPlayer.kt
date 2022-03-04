package ui.player

import com.tajmoti.libtulip.ui.streams.LoadedLink
import react.Props
import react.dom.video
import react.fc

internal external interface HtmlVideoPlayerProps : Props {
    var link: LoadedLink
}

internal val HtmlVideoPlayer = fc<HtmlVideoPlayerProps> { (link) ->
    video("w-100") {
        attrs.src = link.directLink
        attrs.controls = true
        attrs.autoPlay = true
    }
}


private operator fun HtmlVideoPlayerProps.component1() = link
