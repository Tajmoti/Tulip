package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed interface TulipItem {
    val key: ItemKey
    val name: String?

    sealed interface Tmdb : TulipItem {
        override val key: ItemKey.Tmdb
        override val name: String
        val overview: String?
        val posterPath: String?
        val backdropPath: String?
    }

    sealed interface Hosted : TulipItem {
        override val key: ItemKey.Hosted
        val language: LanguageCode
        val tmdbId: ItemKey.Tmdb?
    }
}