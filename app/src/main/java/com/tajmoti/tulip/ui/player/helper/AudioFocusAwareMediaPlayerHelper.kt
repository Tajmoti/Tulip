package com.tajmoti.tulip.ui.player.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.*
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper
import com.tajmoti.libtulip.ui.player.MediaPlayerState

abstract class AudioFocusAwareMediaPlayerHelper(
    private val context: Context,
    private val delegate: MediaPlayerHelper,
) : MediaPlayerHelper {
    override val videoUrl = delegate.videoUrl
    override val state = delegate.state
    override var progress: Float
        get() {
            return delegate.progress
        }
        set(value) {
            delegate.progress = value
        }
    override var time: Long
        get() {
            return delegate.time
        }
        set(value) {
            delegate.time = value
        }
    override val length = delegate.length

    private val receiver = BecomingNoisyReceiver()
    private var registered = false

    /**
     * Callback to receive audio focus changes.
     */
    protected var callback = OnAudioFocusChangeListener { onAudioFocusChanged(it) }

    /**
     * Request audio focus and return e.g. [AUDIOFOCUS_REQUEST_GRANTED].
     */
    abstract fun requestAudioFocus(): Int

    /**
     * Abandons audio focus requested with [requestAudioFocus].
     */
    abstract fun abandonAudioFocus()


    override fun play() {
        if (requestAudioFocus() != AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }
        if (!registered) {
            context.registerReceiver(receiver, IntentFilter(ACTION_AUDIO_BECOMING_NOISY))
            registered = true
        }
        delegate.play()
    }

    override fun pause() {
        stopAudioFocusHandling()
        delegate.pause()
    }

    private fun stopAudioFocusHandling() {
        if (registered) {
            context.unregisterReceiver(receiver)
            registered = false
        }
        abandonAudioFocus()
    }

    override fun playOrPause() {
        if (state.value is MediaPlayerState.Playing) {
            pause()
        } else {
            play()
        }
    }

    override fun setSubtitles(info: MediaPlayerHelper.SubtitleInfo?) {
        delegate.setSubtitles(info)
    }

    override fun setSubtitleDelay(delay: Long): Boolean {
        return delegate.setSubtitleDelay(delay)
    }

    private fun onAudioFocusChanged(focus: Int) {
        when (focus) {
            AUDIOFOCUS_LOSS -> pause()
        }
    }

    override fun release() {
        stopAudioFocusHandling()
        delegate.release()
    }

    private inner class BecomingNoisyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }
}
