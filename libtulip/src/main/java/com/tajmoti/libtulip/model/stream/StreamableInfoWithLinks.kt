package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.StreamableInfo

data class StreamableInfoWithLinks(
    val info: StreamableInfo,
    val streams: List<UnloadedVideoStreamRef>
)