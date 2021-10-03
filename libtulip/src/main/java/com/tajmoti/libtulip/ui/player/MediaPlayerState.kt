package com.tajmoti.libtulip.ui.player

sealed interface MediaPlayerState {
    /**
     * No media is selected.
     */
    object Idle : MediaPlayerState

    /**
     * Some media is actively being played.
     */
    class Playing(val position: Position) : MediaPlayerState

    /**
     * Some media is paused.
     */
    class Paused(val position: Position) : MediaPlayerState

    /**
     * Some media is buffering.
     */
    class Buffering(val position: Position, val percent: Float) : MediaPlayerState

    /**
     * The media is done playing.
     */
    object Finished: MediaPlayerState

    /**
     * There is an error with the player or media.
     */
    object Error : MediaPlayerState
}