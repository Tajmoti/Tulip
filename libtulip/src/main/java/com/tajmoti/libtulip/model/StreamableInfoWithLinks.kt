package com.tajmoti.libtulip.model

data class StreamableInfoWithLinks(
    val info: StreamableInfo,
    val streams: List<UnloadedVideoStreamRef>
)