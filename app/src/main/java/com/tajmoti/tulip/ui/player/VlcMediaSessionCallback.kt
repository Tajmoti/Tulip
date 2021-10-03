package com.tajmoti.tulip.ui.player

import android.support.v4.media.session.MediaSessionCompat

class VlcMediaSessionCallback(
    private val vlc: VlcMediaHelper
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