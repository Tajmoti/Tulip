package com.tajmoti.libtulip.ui.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Postpones applying of [position] and [time] values until the [delegate] gets to the [MediaPlayerState.Playing] state.
 */
class PositionDelayingWrapper(
    private val delegate: VideoPlayer,
    scope: CoroutineScope
) : VideoPlayer by delegate {
    /**
     * Time that the media should skip to ASAP.
     * Handles situations where the [time] or [position] is attempted to be
     * set earlier than the player can actually apply them.
     */
    private var timeToSet: MediaPosition? = null

    /**
     * True if the player was in the [MediaPlayerState.Playing] at least once.
     */
    private var wasAlreadyPlaying = false


    init {
        scope.launch {
            state.collect {
                if (it is MediaPlayerState.Playing) {
                    setSavedPositionOnce()
                }
            }
        }
    }

    private fun setSavedPositionOnce() {
        if (wasAlreadyPlaying) return
        wasAlreadyPlaying = true
        val tts = timeToSet ?: return
        when (tts) {
            is MediaPosition.Position -> position = tts.position
            is MediaPosition.Time -> time = tts.timeMs
        }
    }


    override var position: Float
        get() = delegate.position
        set(value) = if (wasAlreadyPlaying) {
            delegate.position = value
        } else {
            timeToSet = MediaPosition.Position(value)
        }


    override var time: Long
        get() = delegate.time
        set(value) = if (wasAlreadyPlaying) {
            delegate.time = value
        } else {
            timeToSet = MediaPosition.Time(value)
        }

    sealed interface MediaPosition {
        class Position(val position: Float) : MediaPosition
        class Time(val timeMs: Long) : MediaPosition
    }
}