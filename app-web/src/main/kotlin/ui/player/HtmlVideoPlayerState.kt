package ui.player

import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.VideoPlayer
import kotlinx.coroutines.flow.MutableStateFlow

class HtmlVideoPlayerState : VideoPlayer {
    private val _state = MutableStateFlow<MediaPlayerState>(MediaPlayerState.Idle)
    private var _initialProgress: Float? = null
    override val videoUrl = ""
    override val state = _state
    override val length = 0L
    /**
     * Progress to set to the video player on first playback.
     */
    val initialProgress: Float?
        get() = _initialProgress


    override fun setProgress(progress: Float) {
        _initialProgress = progress
    }

    override fun setTime(time: Long) {

    }

    override fun play() {

    }

    override fun pause() {

    }

    override fun playOrPause() {

    }

    override fun setSubtitles(info: VideoPlayer.SubtitleInfo?) {

    }

    override fun setSubtitleDelay(delay: Long): Boolean {
        return false
    }

    override fun release() {

    }

    fun updateState(state: MediaPlayerState) {
        _state.value = state
    }
}