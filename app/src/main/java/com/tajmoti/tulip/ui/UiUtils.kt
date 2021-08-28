@file:Suppress("UNUSED")

package com.tajmoti.tulip.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.tulip.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun languageToIcon(language: LanguageCode): Int? {
    return when (language.code) {
        "en" -> R.drawable.ic_flag_uk
        "de" -> R.drawable.ic_flag_de
        else -> null
    }
}

fun LifecycleOwner.consume(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

fun Fragment.consume(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}