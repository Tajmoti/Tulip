package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.TvItem

data class TulipTvShow(
    override val service: StreamingService,
    override val key: String,
    override val name: String,
    override val language: String,
    override val firstAirDateYear: Int?,
    override val tmdbId: TmdbId?
) : TulipSearchResult() {
    constructor(service: StreamingService, item: TvItem.Show, tmdbId: TmdbId?)
            : this(service, item.key, item.name, item.language, item.firstAirDateYear, tmdbId)

    val apiInfo: TvItem.Show.Info
        get() = TvItem.Show.Info(key, name, language, firstAirDateYear)
}