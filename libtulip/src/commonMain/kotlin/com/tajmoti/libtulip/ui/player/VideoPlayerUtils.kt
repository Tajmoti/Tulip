package com.tajmoti.libtulip.ui.player

import com.tajmoti.libtulip.model.info.*
import kotlin.jvm.JvmStatic
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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
    fun episodeToLabel(episodeInfo: Episode): String {
        val name = episodeInfo.name ?: ""
        return "${episodeInfo.episodeNumber}. $name"
    }

    @JvmStatic
    fun showToDisplayName(item: TulipCompleteEpisodeInfo): String {
        val showSeasonEpNum = "${item.tvShow.name} S${item.season.seasonNumber}:E${item.episodeNumber}"
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
        val hours = mutableTimeMs.milliseconds.inWholeHours
        mutableTimeMs -= hours.hours.inWholeMilliseconds
        val minutes = mutableTimeMs.milliseconds.inWholeMinutes
        mutableTimeMs -= minutes.minutes.inWholeMilliseconds
        val seconds = mutableTimeMs.milliseconds.inWholeSeconds
        return when {
            hours > 0 -> hours.toString() + ':' + timePad(minutes) + ':' + timePad(seconds)
            minutes > 0 -> timePad(minutes) + ':' + timePad(seconds)
            else -> "0:" + timePad(seconds)
        }
    }
}