package com.tajmoti.tulip.ui.player

import android.net.Uri
import com.tajmoti.commonutils.logger
import kotlinx.coroutines.flow.MutableStateFlow
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class VlcMediaHelper(
    lib: LibVLC,
    private val videoUrl: String
) : MediaPlayer.EventListener, MediaPlayerHelper {
    /**
     * Current state of the player and the media being played
     */
    override val state = MutableStateFlow<MediaPlayerHelper.State>(
        MediaPlayerHelper.State.Initializing
    )

    /**
     * Video player of the media being played
     */
    private val player = setupPlayer(lib)

    /**
     * Prevent double-attaches
     */
    private var attached = false


    fun attachAndPlay(view: VLCVideoLayout) {
        if (!attached)
            player.attachViews(view, null, true, false)
        attached = true
        player.play()
    }

    fun detachAndPause() {
        player.pause()
        player.detachViews()
        attached = false
    }

    override fun playOrResume() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun setProgress(progress: Float) {
        player.time = (progress * player.length).toLong()
    }

    override fun setSubtitles(uri: Uri) {
        player.addSlave(Media.Slave.Type.Subtitle, uri, true)
    }

    override fun setSubtitleDelay(delay: Long): Boolean {
        return delay == 0L || (player.isPlaying && player.setSpuDelay(-delay * 1000))
    }

    override fun getTime(): Long {
        return player.time
    }

    fun release() {
        player.detachViews()
        attached = false
        player.release()
    }

    override fun onEvent(event: MediaPlayer.Event) {
        if (!event.isSpam)
            logger.debug("VLC event ${event.format()}")
        val state = when (event.type) {
            MediaPlayer.Event.PositionChanged ->
                MediaPlayerHelper.State.Playing(Position(event.positionChanged, player.time))
            MediaPlayer.Event.Buffering ->
                MediaPlayerHelper.State.Buffering(
                    Position(player.position, player.time),
                    event.buffering
                )
            MediaPlayer.Event.Playing ->
                MediaPlayerHelper.State.Playing(Position(player.position, player.time))
            MediaPlayer.Event.Paused ->
                MediaPlayerHelper.State.Paused(Position(player.position, player.time))
            MediaPlayer.Event.Stopped ->
                MediaPlayerHelper.State.Error
            MediaPlayer.Event.EncounteredError ->
                MediaPlayerHelper.State.Error
            else -> null
        }
        state?.let { this.state.value = it }
    }


    private fun setupPlayer(lib: LibVLC): MediaPlayer {
        return MediaPlayer(lib).also { setMedia(lib, it, Uri.parse(videoUrl)) }
    }

    private fun setMedia(lib: LibVLC, player: MediaPlayer, uri: Uri) {
        val media = Media(lib, uri)
        player.media = media
        media.release()
    }

    init {
        player.setEventListener(this)
    }
}