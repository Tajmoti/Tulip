package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.StreamingService

data class SeasonKey(
    val service: StreamingService,
    val tvShowId: String,
    val seasonId: String
)
