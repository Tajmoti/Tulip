package com.tajmoti.tulip.ui.player

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.tulip.ui.showToDisplayName
import java.util.concurrent.TimeUnit

object VideoPlayerUtils {

    fun formatTimeForDisplay(timeMs: Long): String {
        var mutableTimeMs = timeMs
        val hours = TimeUnit.MILLISECONDS.toHours(mutableTimeMs)
        mutableTimeMs -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(mutableTimeMs)
        mutableTimeMs -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(mutableTimeMs)
        return when {
            hours > 0 -> hours.toString() + ':' + timePad(minutes) + ':' + timePad(seconds)
            minutes > 0 -> timePad(minutes) + ':' + timePad(seconds)
            else -> "0:" + timePad(seconds)
        }
    }

    fun streamableToDisplayName(info: StreamableInfo): String {
        return when (info) {
            is TulipCompleteEpisodeInfo -> showToDisplayName(info)
            is TulipMovie -> info.name
        }
    }

    private fun timePad(time: Long): String {
        return time.toString().padStart(2, '0')
    }
}