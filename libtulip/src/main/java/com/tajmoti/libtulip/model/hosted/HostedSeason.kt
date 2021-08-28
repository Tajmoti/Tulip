package com.tajmoti.libtulip.model.hosted

data class HostedSeason(
    val service: StreamingService,
    val tvShowKey: String,
    val number: Int
)