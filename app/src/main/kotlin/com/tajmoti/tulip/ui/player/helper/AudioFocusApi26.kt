package com.tajmoti.tulip.ui.player.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper

@RequiresApi(Build.VERSION_CODES.O)
class AudioFocusApi26(
    context: Context,
    private val audioManager: AudioManager,
    delegate: MediaPlayerHelper
) : AudioFocusAwareMediaPlayerHelper(context, delegate) {
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
        .build()

    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener(callback)
        .build()

    override fun requestAudioFocus(): Int {
        return audioManager.requestAudioFocus(request)
    }

    override fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(request)
    }
}