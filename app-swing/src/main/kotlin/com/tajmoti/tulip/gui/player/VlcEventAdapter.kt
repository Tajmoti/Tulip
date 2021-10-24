package com.tajmoti.tulip.gui.player

import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.Position
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter

class VlcEventAdapter(
    /**
     * Consumer of events produced by the player.
     * Invoked on the libvlc native thread!
     */
    val updateStateOffThread: (MediaPlayerState) -> Unit
) : MediaPlayerEventAdapter() {
    override fun buffering(p: MediaPlayer, newCache: Float) {
        val newState = MediaPlayerState.Buffering(getCurrentPosition(p), newCache / 100.0f)
        updateStateOffThread(newState)
    }

    override fun playing(p: MediaPlayer) {
        val newState = MediaPlayerState.Paused(getCurrentPosition(p))
        updateStateOffThread(newState)
    }

    override fun paused(p: MediaPlayer) {
        val newState = MediaPlayerState.Paused(getCurrentPosition(p))
        updateStateOffThread(newState)
    }

    override fun finished(p: MediaPlayer) {
        val newState = MediaPlayerState.Finished
        updateStateOffThread(newState)
    }

    override fun positionChanged(p: MediaPlayer, newPosition: Float) {
        val position = p.submitGet { Position(newPosition, p.status().time()) }
        val newState = MediaPlayerState.Playing(position)
        updateStateOffThread(newState)
    }

    override fun error(mediaPlayer: MediaPlayer) {
        val newState = MediaPlayerState.Error
        updateStateOffThread(newState)
    }

    private fun getCurrentPosition(player: MediaPlayer): Position {
        return player.submitGet { Position(status().position(), status().time()) }
    }
}