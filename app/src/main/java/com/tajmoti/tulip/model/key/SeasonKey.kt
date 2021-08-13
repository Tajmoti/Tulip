package com.tajmoti.tulip.model.key

import com.tajmoti.tulip.model.StreamingService

data class SeasonKey(
    val service: StreamingService,
    val tvShowId: String,
    val seasonId: String
)
