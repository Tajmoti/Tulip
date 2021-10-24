package com.tajmoti.tulip.gui.player

import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.VideoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.caprica.vlcj.media.MediaSlaveType
import uk.co.caprica.vlcj.player.base.MediaPlayer
import java.util.concurrent.Executor

class DesktopVlcVideoPlayer(
    private val player: MediaPlayer,
    override val videoUrl: String,
    private val nativeExecutor: Executor
) : VideoPlayer {
    override val state = MutableStateFlow<MediaPlayerState>(MediaPlayerState.Idle)

    init {
        player.submitOn {
            events().addMediaPlayerEventListener(VlcEventAdapter(this@DesktopVlcVideoPlayer::updateStateOffThread))
            media().play(videoUrl)
        }
    }


    override fun play() {
        player.submitOn { controls().play() }
    }

    override fun pause() {
        player.submitOn { controls().pause() }
    }

    override fun playOrPause() {
        val isPlaying = isPlaying()
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    private fun isPlaying(): Boolean {
        return player.submitGet { status().isPlaying }
    }

    override var position: Float
        get() = player.submitGet { status().position() }
        set(value) = player.submitOn { controls().setPosition(value) }

    override var time: Long
        get() = player.submitGet { status().time() }
        set(value) = player.submitOn { controls().setTime(value) }

    override val length: Long
        get() = player.submitGet { status().length() }

    override fun setSubtitles(info: VideoPlayer.SubtitleInfo?) {
        player.submitOn {
            if (info != null) {
                val (uri, encoding) = info // TODO Encoding
                media().addSlave(MediaSlaveType.SUBTITLE, uri, true)
            } else {
                media().slaves().clear()
            }
        }
    }

    override fun setSubtitleDelay(delay: Long): Boolean {
        return if (delay == 0L) {
            true
        } else if (isPlaying()) {
            player.submitOn { subpictures().setDelay(delay * 1000) }
            true
        } else {
            false
        }
    }

    override fun release() {

    }

    /**
     * Posts an update to the player state on the GUI thread.
     * Prevents threading issues caused by updating GUI state
     * from the libvlc native thread.
     */
    private fun updateStateOffThread(newState: MediaPlayerState) {
        nativeExecutor.execute { state.value = newState }
    }
}