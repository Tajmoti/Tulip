package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.TvItem

data class TulipMovie(
    override val service: StreamingService,
    override val key: String,
    override val name: String,
    override val language: String,
    override val firstAirDateYear: Int?,
    override val tmdbId: TmdbId?
) : TulipSearchResult() {
    constructor(service: StreamingService, movie: TvItem.Movie, tmdbId: TmdbId?)
            : this(service, movie.key, movie.name, movie.language, movie.firstAirDateYear, tmdbId)

    val apiInfo: TvItem.Movie.Info
        get() = TvItem.Movie.Info(key, name, language, firstAirDateYear)
}