package com.tajmoti.tulip.model.key

import com.tajmoti.tulip.model.StreamingService

data class MovieKey(
    override val service: StreamingService,
    val movieId: String
) : StreamableKey()
