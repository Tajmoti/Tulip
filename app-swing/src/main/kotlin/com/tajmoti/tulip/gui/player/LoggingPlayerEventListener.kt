package com.tajmoti.tulip.gui.player

import org.slf4j.LoggerFactory
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.media.TrackType
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener

class LoggingPlayerEventListener(
    private val disableSpammyEvents: Boolean = true
) : MediaPlayerEventListener {
    private val logger = LoggerFactory.getLogger(LoggingPlayerEventListener::class.java)


    override fun mediaChanged(mediaPlayer: MediaPlayer?, media: MediaRef?) {
        logger.debug("Player event mediaChanged : mediaPlayer: MediaPlayer?, media: MediaRef?")
    }

    override fun opening(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event opening : mediaPlayer: MediaPlayer?")
    }

    override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
        logger.debug("Player event buffering : mediaPlayer: MediaPlayer?, newCache: Float")
    }

    override fun playing(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event playing : mediaPlayer: MediaPlayer?")
    }

    override fun paused(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event paused : mediaPlayer: MediaPlayer?")
    }

    override fun stopped(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event stopped : mediaPlayer: MediaPlayer?")
    }

    override fun forward(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event forward : mediaPlayer: MediaPlayer?")
    }

    override fun backward(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event backward : mediaPlayer: MediaPlayer?")
    }

    override fun finished(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event finished : mediaPlayer: MediaPlayer?")
    }

    override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
        if (disableSpammyEvents) return
        logger.debug("Player event timeChanged : mediaPlayer: MediaPlayer?, newTime: Long")
    }

    override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
        if (disableSpammyEvents) return
        logger.debug("Player event positionChanged : mediaPlayer: MediaPlayer?, newPosition: Float")
    }

    override fun seekableChanged(mediaPlayer: MediaPlayer?, newSeekable: Int) {
        logger.debug("Player event seekableChanged : mediaPlayer: MediaPlayer?, newSeekable: Int")
    }

    override fun pausableChanged(mediaPlayer: MediaPlayer?, newPausable: Int) {
        logger.debug("Player event pausableChanged : mediaPlayer: MediaPlayer?, newPausable: Int")
    }

    override fun titleChanged(mediaPlayer: MediaPlayer?, newTitle: Int) {
        logger.debug("Player event titleChanged : mediaPlayer: MediaPlayer?, newTitle: Int")
    }

    override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {
        logger.debug("Player event snapshotTaken : mediaPlayer: MediaPlayer?, filename: String?")
    }

    override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
        logger.debug("Player event lengthChanged : mediaPlayer: MediaPlayer?, newLength: Long")
    }

    override fun videoOutput(mediaPlayer: MediaPlayer?, newCount: Int) {
        logger.debug("Player event videoOutput : mediaPlayer: MediaPlayer?, newCount: Int")
    }

    override fun scrambledChanged(mediaPlayer: MediaPlayer?, newScrambled: Int) {
        logger.debug("Player event scrambledChanged : mediaPlayer: MediaPlayer?, newScrambled: Int")
    }

    override fun elementaryStreamAdded(mediaPlayer: MediaPlayer?, type: TrackType?, id: Int) {
        logger.debug("Player event elementaryStreamAdded : mediaPlayer: MediaPlayer?, type: TrackType?, id: Int")
    }

    override fun elementaryStreamDeleted(mediaPlayer: MediaPlayer?, type: TrackType?, id: Int) {
        logger.debug("Player event elementaryStreamDeleted : mediaPlayer: MediaPlayer?, type: TrackType?, id: Int")
    }

    override fun elementaryStreamSelected(mediaPlayer: MediaPlayer?, type: TrackType?, id: Int) {
        logger.debug("Player event elementaryStreamSelected : mediaPlayer: MediaPlayer?, type: TrackType?, id: Int")
    }

    override fun corked(mediaPlayer: MediaPlayer?, corked: Boolean) {
        logger.debug("Player event corked : mediaPlayer: MediaPlayer?, corked: Boolean")
    }

    override fun muted(mediaPlayer: MediaPlayer?, muted: Boolean) {
        logger.debug("Player event muted : mediaPlayer: MediaPlayer?, muted: Boolean")
    }

    override fun volumeChanged(mediaPlayer: MediaPlayer?, volume: Float) {
        logger.debug("Player event volumeChanged : mediaPlayer: MediaPlayer?, volume: Float")
    }

    override fun audioDeviceChanged(mediaPlayer: MediaPlayer?, audioDevice: String?) {
        logger.debug("Player event audioDeviceChanged : mediaPlayer: MediaPlayer?, audioDevice: String?")
    }

    override fun chapterChanged(mediaPlayer: MediaPlayer?, newChapter: Int) {
        logger.debug("Player event chapterChanged : mediaPlayer: MediaPlayer?, newChapter: Int")
    }

    override fun error(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event error : mediaPlayer: MediaPlayer?")
    }

    override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
        logger.debug("Player event mediaPlayerReady : mediaPlayer: MediaPlayer?")
    }
}