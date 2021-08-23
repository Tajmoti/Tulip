package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.key.MovieKey

data class HostedMovie(
    val service: StreamingService,
    val key: String,
    val name: String,
    val language: String
) : HostedStreamable {
    override val hostedKey = MovieKey.Hosted(service, key)
}