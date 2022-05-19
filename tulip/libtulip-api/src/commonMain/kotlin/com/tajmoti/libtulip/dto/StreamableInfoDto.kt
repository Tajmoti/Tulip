package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.StreamableKey

sealed interface StreamableInfoDto {
    val key: StreamableKey
    val name: String?

    sealed interface Tmdb : StreamableInfoDto {
        override val key: StreamableKey.Tmdb
    }

    sealed interface Hosted : StreamableInfoDto {
        override val key: StreamableKey.Hosted
        val language: LanguageCodeDto
    }
}