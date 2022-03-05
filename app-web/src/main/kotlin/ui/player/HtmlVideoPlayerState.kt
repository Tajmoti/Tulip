package ui.player

import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.VideoPlayer
import kotlinx.coroutines.flow.MutableStateFlow

class HtmlVideoPlayerState : VideoPlayer {
    private val _state = MutableStateFlow<MediaPlayerState>(MediaPlayerState.Idle)
    override val videoUrl = ""
    override val state = _state
    override var position: Float
        get() = 0.0f
        set(_) {}
    override var time: Long
        get() = 0
        set(_) {}
    override val length = 0L

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