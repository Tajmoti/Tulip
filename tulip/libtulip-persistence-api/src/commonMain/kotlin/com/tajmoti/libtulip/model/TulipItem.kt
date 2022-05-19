package com.tajmoti.libtulip.model

import com.tajmoti.libtulip.model.key.ItemKey

sealed interface TulipItem {
    val key: ItemKey
    val name: String

    sealed interface Tmdb : TulipItem {
        override val key: ItemKey.Tmdb
        override val name: String
        val overview: String?
        val posterUrl: String?
        val backdropUrl: String?
    }

    sealed interface Hosted : TulipItem {
        override val key: ItemKey.Hosted
        val language: LanguageCode
        val tmdbId: ItemKey.Tmdb?
    }
}