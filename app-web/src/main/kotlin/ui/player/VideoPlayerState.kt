package ui.player

import react.State

external interface VideoPlayerState : State {
    var status: VideoPlayerStatus
}