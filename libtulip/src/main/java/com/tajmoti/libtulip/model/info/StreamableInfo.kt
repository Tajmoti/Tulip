package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.StreamableKey

sealed interface StreamableInfo {
    val key: StreamableKey
    val name: String?

    sealed interface Tmdb : StreamableInfo {
        override val key: StreamableKey.Tmdb
    }

    sealed interface Hosted : StreamableInfo {
        override val key: StreamableKey.Hosted
        val language: LanguageCode
    }
}