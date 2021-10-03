package com.tajmoti.libtulip.ui.player

import kotlinx.coroutines.flow.MutableStateFlow

interface MediaPlayerHelper {
    /**
     * Current state of the player and the media being played
     */
    val state: MutableStateFlow<MediaPlayerState>

    fun play()
    fun pause()
    fun playOrPause()
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