package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import react.Props

external interface LinkListProps : Props {
    var viewModel: VideoPlayerViewModel
    var itemCallback: (UnloadedVideoStreamRef) -> Unit
}