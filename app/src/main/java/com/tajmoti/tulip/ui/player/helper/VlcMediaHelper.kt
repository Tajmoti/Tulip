package com.tajmoti.tulip.ui.player.helper

import android.net.Uri
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.ui.player.MediaPlayerHelper
import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.Position
import com.tajmoti.tulip.ui.player.format
import com.tajmoti.tulip.ui.player.isSpam
import kotlinx.coroutines.flow.MutableStateFlow
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.ILibVLC
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.VLCVideoLayout

class VlcMediaHelper(
    lib: ILibVLC,
    override val videoUrl: String
) : MediaPlayer.EventListener, MediaPlayerHelper {
    /**
     * Current state of the player and the media being played
     */
    override val state = MutableStateFlow<MediaPlayerState>(
        MediaPlayerState.Idle
    )

    /**
     * Video player of the media being played
     */
    private val player = setupPlayer(lib)

    /**
     * Prevent double-attaches
     */
    private var attached = false

    /**
     * Time that the media should seek to ASAP.
     */
    private var timeToSet: MediaPosition? = null


    fun attach(view: VLCVideoLayout) {
        if (!attached)
            player.attachViews(view, null, true, false)
        attached = true
    }

    fun detachAndPause() {
        player.pause()
        player.detachViews()
        attached = false
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun playOrPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override var progress: Float
        get() = player.time.toFloat() / player.length.toFloat()
        set(value) {
            if (player.isPlaying) {
                player.time = (value * player.length).toLong()
            } else {
                timeToSet = MediaPosition.Fraction(value)
            }
        }

    override var time: Long
        get() = player.time
        set(value) {
            if (player.isPlaying) {
                player.time = value
            } else {
                timeToSet = MediaPosition.AbsoluteTime(value)
            }
        }

    override val length: Long
        get() = player.length

    override fun setSubtitles(info: MediaPlayerHelper.SubtitleInfo?) {
        if (info != null) {
            val (uri, encoding) = info
            encoding?.let { player.media?.addOption(":subsdec-encoding=$encoding") }
            player.addSlave(IMedia.Slave.Type.Subtitle, Uri.parse(uri), true)
        } else {
            player.spuTrack = -1
        }
    }

    override fun setSubtitleDelay(delay: Long): Boolean {
        return delay == 0L || (player.isPlaying && player.setSpuDelay(delay * 1000))
    }

    override fun release() {
        player.detachViews()
        attached = false
        player.release()
    }

    override fun onEvent(event: MediaPlayer.Event) {
        if (!event.isSpam)
            logger.debug("VLC event ${event.format()}")
        val newState = when (event.type) {
            MediaPlayer.Event.PositionChanged ->
                MediaPlayerState.Playing(Position(event.positionChanged, player.time))
            MediaPlayer.Event.Buffering -> {
                // We don't care about buffering events when the video is paused,
                // only when it's in the playing state
                if (state.value is MediaPlayerState.Paused) {
                    null
                } else {
                    MediaPlayerState.Buffering(
                        Position(player.position, player.time),
                        event.buffering
                    )
                }
            }
            MediaPlayer.Event.Playing -> {
                onPlayingEvent()
                MediaPlayerState.Playing(Position(player.position, player.time))
            }
            MediaPlayer.Event.Paused ->
                MediaPlayerState.Paused(Position(player.position, player.time))
            MediaPlayer.Event.EndReached ->
                MediaPlayerState.Finished
            MediaPlayer.Event.EncounteredError ->
                MediaPlayerState.Error
            else -> null
        }
        newState?.let { state.value = it }
    }

    private fun onPlayingEvent() {
        timeToSet?.let { time = positionToTimeMs(it) }
        timeToSet = null
    }

    private fun positionToTimeMs(it: MediaPosition): Long {
        return when (it) {
            is MediaPosition.AbsoluteTime -> it.timeMs
            is MediaPosition.Fraction -> (it.progress * player.length).toLong()
        }
    }


    private fun setupPlayer(lib: ILibVLC): MediaPlayer {
        return MediaPlayer(lib).also { setMedia(lib, it, Uri.parse(videoUrl)) }
    }

    private fun setMedia(lib: ILibVLC, player: MediaPlayer, uri: Uri) {
        val media = Media(lib, uri)
        player.media = media
        media.release()
    }

    init {
        player.setEventListener(this)
    }

    sealed interface MediaPosition {
        class Fraction(val progress: Float) : MediaPosition
        class AbsoluteTime(val timeMs: Long) : MediaPosition
    }
}