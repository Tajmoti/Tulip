@file:Suppress("UNUSED")

package com.tajmoti.tulip.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
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