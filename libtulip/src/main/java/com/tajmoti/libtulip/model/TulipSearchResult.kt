package com.tajmoti.libtulip.model

sealed class TulipSearchResult {
    abstract val service: StreamingService
    abstract val key: String
    abstract val name: String
    abstract val language: String
    abstract val firstAirDateYear: Int?
    abstract val tmdbId: TmdbId?
}
