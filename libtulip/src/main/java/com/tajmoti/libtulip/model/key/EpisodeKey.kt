package com.tajmoti.libtulip.model.key

import com.tajmoti.libtulip.model.StreamingService

data class EpisodeKey(
    override val service: StreamingService,
    val tvShowId: String,
    val seasonId: String,
    val episodeId: String
) : StreamableKey()
