package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.StreamingService

data class MovieKey(
    override val service: StreamingService,
    val movieId: String
) : StreamableKey()
