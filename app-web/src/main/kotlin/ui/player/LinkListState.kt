package ui.player

import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import react.State

external interface LinkListState : State {
    var state: VideoPlayerViewModel.State
}