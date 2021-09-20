package com.tajmoti.libtulip.ui.player

import kotlinx.coroutines.flow.MutableStateFlow

interface MediaPlayerHelper {
    /**
     * Current state of the player and the media being played
     */
    val state: MutableStateFlow<State>

    sealed interface State {
        object Initializing : State
        class Playing(val position: Position) : State
        class Paused(val position: Position) : State
        class Buffering(val position: Position, val percent: Float) : State
        object Error : State
    }

    fun playOrResume()
    var progress: Float
    var time: Long
    val length: Long
    fun setSubtitles(uri: String?, encoding: String? = null)
    fun setSubtitleDelay(delay: Long): Boolean
}