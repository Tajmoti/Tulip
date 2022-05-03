package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.serialization.Serializable

interface TvShow : TulipItem {
    val seasons: List<Season>

    @Serializable
    data class Tmdb(
        override val key: TvShowKey.Tmdb,
        override val name: String,
        override val overview: String?,
        override val posterUrl: String?,
        override val backdropUrl: String?,
        override val seasons: List<Season.Tmdb>
    ) : TvShow, TulipItem.Tmdb

    @Serializable
    data class Hosted(
        override val key: TvShowKey.Hosted,
        override val name: String,
        override val language: LanguageCode,
        val firstAirDateYear: Int?,
        override val tmdbId: TvShowKey.Tmdb?,
        override val seasons: List<Season.Hosted>
    ) : TvShow, TulipItem.Hosted
}