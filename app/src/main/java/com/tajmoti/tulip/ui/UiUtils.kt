@file:Suppress("UNUSED")

package com.tajmoti.tulip.ui

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.tulip.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

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

fun episodeToLabel(episodeInfo: TulipEpisodeInfo): String {
    val name = episodeInfo.name ?: ""
    return "${episodeInfo.episodeNumber}. $name"
}

fun showToDisplayName(item: TulipCompleteEpisodeInfo): String {
    val showSeasonEpNum = "${item.showName} S${item.seasonNumber}:E${item.episodeNumber}"
    val episodeName = item.name?.let { " '$it'" } ?: ""
    return showSeasonEpNum + episodeName
}

inline val AppCompatActivity.isInPipModeCompat: Boolean
    get() = run {
        var isInPipMode = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            isInPipMode = isInPictureInPictureMode
        isInPipMode
    }

inline fun <T> Fragment.consume(flow: Flow<T>, crossinline action: suspend (value: T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(action)
        }
    }
}

inline fun <T> AppCompatActivity.consume(
    flow: Flow<T>,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(action)
        }
    }
}

fun Fragment.slideToBottomDismiss() {
    requireActivity()
        .supportFragmentManager
        .commit {
            setCustomAnimations(
                R.anim.slide_from_top_enter,
                R.anim.slide_from_top_exit,
                R.anim.slide_from_top_enter,
                R.anim.slide_from_top_exit
            )
            remove(this@slideToBottomDismiss)
        }
}

fun ViewModel.doCancelableJob(
    job: KMutableProperty0<Job?>,
    state: MutableStateFlow<Boolean>?,
    task: suspend () -> Unit
) {
    job.get()?.cancel()
    val newJob = viewModelScope.launch {
        state?.value = true
        try {
            task()
        } finally {
            state?.value = false
        }
    }
    job.set(newJob)
}