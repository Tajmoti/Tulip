@file:Suppress("UNUSED")

package com.tajmoti.tulip.ui

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.ui.player.VideoPlayerUtils.episodeToLabel
import com.tajmoti.tulip.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun languageToIcon(language: LanguageCode): Int? {
    return when (language.code) {
        "en" -> R.drawable.ic_flag_uk
        "de" -> R.drawable.ic_flag_de
        else -> null
    }
}

fun getSeasonTitle(ctx: Context, season: TulipSeasonInfo): String {
    val seasonTitle = if (season.seasonNumber == 0) {
        ctx.getString(R.string.season_specials)
    } else {
        ctx.getString(R.string.season_no, season.seasonNumber)
    }
    return seasonTitle
}

fun showEpisodeDetailsDialog(ctx: Context, episodeInfo: TulipEpisodeInfo) {
    MaterialAlertDialogBuilder(ctx)
        .setIcon(R.drawable.ic_baseline_live_tv_24)
        .setTitle(episodeToLabel(episodeInfo))
        .setMessage(episodeInfo.overview ?: "")
        .setPositiveButton(R.string.dismiss, null)
        .show()
}

inline val AppCompatActivity.isInPipModeCompat: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode

fun <T> Fragment.consume(flow: Flow<T>, collector: FlowCollector<T>) {
    viewLifecycleOwner.consume(flow, collector)
}

fun <T> LifecycleOwner.consume(flow: Flow<T>, collector: FlowCollector<T>) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collector)
        }
    }
}

fun Fragment.slideToBottomDismiss(fm: FragmentManager = requireActivity().supportFragmentManager) {
    fm.commit {
        setCustomAnimations(
            R.anim.slide_from_top_enter,
            R.anim.slide_from_top_exit,
            R.anim.slide_from_top_enter,
            R.anim.slide_from_top_exit
        )
        remove(this@slideToBottomDismiss)
    }
}
