package com.tajmoti.tulip.ui.player

import android.net.Uri
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
    fun setProgress(progress: Float)
    fun setSubtitles(uri: Uri)
    fun setSubtitleDelay(delay: Long): Boolean
    fun getTime(): Long
}