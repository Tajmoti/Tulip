package com.tajmoti.libtulip.ui.player

val MediaPlayerState.positionOrNull: Position?
    get() = when (this) {
        is MediaPlayerState.Buffering -> position
        is MediaPlayerState.Error -> null
        is MediaPlayerState.Idle -> null
        is MediaPlayerState.Paused -> position
        is MediaPlayerState.Playing -> position
        is MediaPlayerState.Finished -> null
    }

val MediaPlayerState.durationOrNull: Long?
    get() = when (this) {
        is MediaPlayerState.Buffering -> duration
        is MediaPlayerState.Error -> null
        is MediaPlayerState.Idle -> null
        is MediaPlayerState.Paused -> duration
        is MediaPlayerState.Playing -> duration
        is MediaPlayerState.Finished -> null
    }

val MediaPlayerState.validPositionOrNull: Position?
    get() = positionOrNull?.takeIf { it.timeMs > 0 && it.fraction > 0.0f }