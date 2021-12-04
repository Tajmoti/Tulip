package com.tajmoti.tulip.ui.player

import org.videolan.libvlc.MediaPlayer

private val VLC_TYPE_TO_NAME = mapOf(
    0x100 to "MediaChanged",
    0x102 to "Opening",
    0x103 to "Buffering",
    0x104 to "Playing",
    0x105 to "Paused",
    0x106 to "Stopped",
    0x109 to "EndReached",
    0x10a to "EncounteredError",
    0x10b to "TimeChanged",
    0x10c to "PositionChanged",
    0x10d to "SeekableChanged",
    0x10e to "PausableChanged",
    0x111 to "LengthChanged",
    0x112 to "Vout",
    0x114 to "ESAdded",
    0x115 to "ESDeleted",
    0x116 to "ESSelected"
)

private val SPAM = setOf(
    MediaPlayer.Event.PositionChanged,
    MediaPlayer.Event.TimeChanged,
    MediaPlayer.Event.ESAdded,
    MediaPlayer.Event.ESDeleted,
    MediaPlayer.Event.ESSelected,
)

fun MediaPlayer.Event.format(): String {
    return "MediaPlayer Event ${VLC_TYPE_TO_NAME[type]} " +
            "timeChanged = $timeChanged, " +
            "lengthChanged = $lengthChanged, " +
            "positionChanged = $positionChanged, " +
            "voutCount = $voutCount, " +
            "pausable = $pausable, " +
            "seekable = $seekable, " +
            "buffering = $buffering"
}

val MediaPlayer.Event.isSpam: Boolean
    get() = SPAM.contains(type)