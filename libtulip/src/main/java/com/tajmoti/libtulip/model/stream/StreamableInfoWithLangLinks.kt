package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.StreamableInfo

data class StreamableInfoWithLangLinks(
    val info: StreamableInfo,
    val streams: List<UnloadedVideoWithLanguage>
)