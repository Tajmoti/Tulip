package com.tajmoti.libtulip.ui.player

import kotlinx.coroutines.flow.MutableStateFlow

interface VideoPlayer {
    /**
     * URL of the video being played by this video player.
     */
    val videoUrl: String

    /**
     * Current state of the player and the media being played.
     */
    val state: MutableStateFlow<MediaPlayerState>


    /**
     * Length of the video in milliseconds.
     */
    val length: Long


    /**
     * Seeks the video to position represented as a fraction from 0.0 to 1.0.
     */
    fun setProgress(progress: Float)

    /**
     * Seeks the video time in milliseconds. Value between 0 and [length].
     */
    fun setTime(time: Long)

    /**
     * Plays the video. Has no effect if the video is already playing or in a state that doesn't allow playback.
     */
    fun play()

    /**
     * Pauses the video. Has no effect if the video is already paused or in a state that doesn't allow pausing.
     */
    fun pause()

    /**
     * Toggles the playing or paused state. Has no effect if they video is not playing,
     * paused, or in a state that would allow playback.
     */
    fun playOrPause()

    /**
     * Loads subtitles specified by [info] or disables subtitles if it's null.
     */
    fun setSubtitles(info: SubtitleInfo?)

    /**
     * Sets the subtitle delay in milliseconds. Has no effect if there are no loaded subtitles.
     */
    fun setSubtitleDelay(delay: Long): Boolean

    /**
     * Releases this video player instance.
     */
    fun release()


    data class SubtitleInfo(
        /**
         * URI to the subtitles.
         */
        val uri: String,
        /**
         * Encoding of the subtitles or null if unknown.
         */
        val encoding: String? = null
    )
}