package com.tajmoti.tulip.model.key

import com.tajmoti.tulip.model.StreamingService

data class EpisodeKey(
    override val service: StreamingService,
    val tvShowId: String,
    val seasonId: String,
    val episodeId: String
) : StreamableKey()
