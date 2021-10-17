package com.tajmoti.tulip.ui.player

import android.support.v4.media.session.MediaSessionCompat
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper

class TulipMediaSessionCallback(
    private val vlc: MediaPlayerHelper
) : MediaSessionCompat.Callback() {

    override fun onPlay() {
        vlc.play()
    }

    override fun onPause() {
        vlc.pause()
    }

    override fun onSeekTo(pos: Long) {
        vlc.time = pos
    }
}