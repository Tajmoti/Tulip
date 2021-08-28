package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.TvItemInfo

sealed class HostedItem {
    abstract val service: StreamingService
    abstract val info: TvItemInfo
    abstract val tmdbId: TmdbItemId?

    data class TvShow(
        override val service: StreamingService,
        override val info: TvItemInfo,
        override val tmdbId: TmdbItemId.Tv?
    ) : HostedItem()

    data class Movie(
        override val service: StreamingService,
        override val info: TvItemInfo,
        override val tmdbId: TmdbItemId.Movie?
    ) : HostedItem()

    val name: String
        get() = info.name
    val language: String
        get() = info.language
    val firstAirDateYear: Int?
        get() = info.firstAirDateYear
}