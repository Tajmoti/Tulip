package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.StreamingService

data class TvShowKey(
    val service: StreamingService,
    val tvShowId: String
)
