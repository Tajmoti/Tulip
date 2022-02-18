package ui.player

import com.tajmoti.libtulip.model.key.StreamableKey
import react.Props

external interface VideoPlayerProps : Props {
    var streamableKey: StreamableKey
}