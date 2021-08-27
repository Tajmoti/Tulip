package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage

data class StreamableInfoWithLangLinks(
    val info: StreamableInfo,
    val streams: List<UnloadedVideoWithLanguage>
)