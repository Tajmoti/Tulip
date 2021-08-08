package com.tajmoti.tulip.model

sealed class StreamableIdentifier(
    val service: StreamingService
) {
    class Movie(
        service: StreamingService,
        val key: String
    ) : StreamableIdentifier(service)

    class TvShow(
        service: StreamingService,
        val tvShow: String,
        val season: String,
        val key: String
    ) : StreamableIdentifier(service)
}