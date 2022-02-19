package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtvprovider.model.VideoStreamRef
import react.State

external interface LinkListState : State {
    var items: List<UnloadedVideoStreamRef>
    var playingLink: VideoStreamRef?
}