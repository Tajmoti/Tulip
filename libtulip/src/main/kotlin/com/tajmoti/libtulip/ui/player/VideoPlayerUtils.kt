package com.tajmoti.libtulip.ui.player

import com.tajmoti.libtulip.model.info.*
import java.util.concurrent.TimeUnit

object VideoPlayerUtils {
    @JvmStatic
    fun streamableToDisplayName(info: StreamableInfo?): String {
        return when (info) {
            is TulipCompleteEpisodeInfo -> showToDisplayName(info)
            is TulipMovie -> info.name
            else -> ""
        }
    }

    @JvmStatic
    fun episodeToLabel(episodeInfo: TulipEpisodeInfo): String {
        val name = episodeInfo.name ?: ""
        return "${episodeInfo.episodeNumber}. $name"
    }

    @JvmStatic
    fun showToDisplayName(item: TulipCompleteEpisodeInfo): String {
        val showSeasonEpNum = "${item.name} S${item.seasonNumber}:E${item.episodeNumber}"
        val episodeName = item.name?.let { " '$it'" } ?: ""
        return showSeasonEpNum + episodeName
    }

    @JvmStatic
    private fun timePad(time: Long): String {
        return time.toString().padStart(2, '0')
    }

    @JvmStatic
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
}