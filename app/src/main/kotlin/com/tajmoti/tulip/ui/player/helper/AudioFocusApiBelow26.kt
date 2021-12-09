package com.tajmoti.tulip.ui.player.helper

import android.content.Context
import android.media.AudioManager
import com.tajmoti.libtulip.ui.player.VideoPlayer

@Suppress("DEPRECATION")
class AudioFocusApiBelow26(
    context: Context,
    private val audioManager: AudioManager,
    delegate: VideoPlayer
) : AudioFocusAwareVideoPlayer(context, delegate) {
    override fun requestAudioFocus(): Int {
        return audioManager.requestAudioFocus(
            callback,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(callback)
    }
}