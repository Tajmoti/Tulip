package com.tajmoti.tulip.ui

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.dto.LanguageCodeDto
import com.tajmoti.libtulip.dto.SeasonEpisodeDto
import com.tajmoti.libtulip.dto.TvShowSeasonDto
import com.tajmoti.libtulip.ui.player.VideoPlayerUtils
import com.tajmoti.tulip.R

fun languageToIcon(language: LanguageCodeDto): Int? {
    return when (language.code) {
        "en" -> R.drawable.ic_flag_uk
        "de" -> R.drawable.ic_flag_de
        else -> null
    }
}

fun getSeasonTitle(ctx: Context, season: TvShowSeasonDto): String {
    val seasonTitle = if (season.seasonNumber == 0) {
        ctx.getString(R.string.season_specials)
    } else {
        ctx.getString(R.string.season_no, season.seasonNumber)
    }
    return seasonTitle
}

fun showEpisodeDetailsDialog(ctx: Context, episodeInfo: SeasonEpisodeDto) {
    MaterialAlertDialogBuilder(ctx)
        .setIcon(R.drawable.ic_baseline_live_tv_24)
        .setTitle(VideoPlayerUtils.episodeToLabel(episodeInfo))
        .setMessage(episodeInfo.overview ?: "")
        .setPositiveButton(R.string.dismiss, null)
        .show()
}
