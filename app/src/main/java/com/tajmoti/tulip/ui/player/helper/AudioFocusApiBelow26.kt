package com.tajmoti.tulip.ui.player.helper

import android.content.Context
import android.media.AudioManager
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper

@Suppress("DEPRECATION")
class AudioFocusApiBelow26(
    context: Context,
    private val audioManager: AudioManager,
    delegate: MediaPlayerHelper
) : AudioFocusAwareMediaPlayerHelper(context, delegate) {
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