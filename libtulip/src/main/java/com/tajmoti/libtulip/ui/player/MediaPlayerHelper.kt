package com.tajmoti.libtulip.ui.player

import kotlinx.coroutines.flow.MutableStateFlow

interface MediaPlayerHelper {
    /**
     * Current state of the player and the media being played
     */
    val state: MutableStateFlow<State>

    sealed interface State {
        /**
         * No media is selected.
         */
        object Idle : State

        /**
         * Some media is actively being played.
         */
        class Playing(val position: Position) : State

        /**
         * Some media is paused.
         */
        class Paused(val position: Position) : State

        /**
         * Some media is buffering.
         */
        class Buffering(val position: Position, val percent: Float) : State

        /**
         * The media is done playing.
         */
        object Finished: State

        /**
         * There is an error with the player or media.
         */
        object Error : State
    }

    fun playOrResume()
    var progress: Float
    var time: Long
    val length: Long
    fun setSubtitles(info: SubtitleInfo?)
    fun setSubtitleDelay(delay: Long): Boolean

    data class SubtitleInfo(
        val uri: String,
        val encoding: String? = null
    )
}