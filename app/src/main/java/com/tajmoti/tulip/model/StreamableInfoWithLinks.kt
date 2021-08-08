package com.tajmoti.tulip.model

import com.tajmoti.tulip.ui.streams.UnloadedVideoStreamRef

data class StreamableInfoWithLinks(
    val info: StreamableInfo,
    val streams: List<UnloadedVideoStreamRef>
)