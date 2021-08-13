package com.tajmoti.tulip.model.key

import com.tajmoti.tulip.model.StreamingService

data class TvShowKey(
    val service: StreamingService,
    val tvShowId: String
)
