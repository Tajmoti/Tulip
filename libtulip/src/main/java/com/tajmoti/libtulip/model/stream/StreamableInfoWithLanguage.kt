package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.StreamableInfo

data class StreamableInfoWithLanguage(
    val streamableInfo: StreamableInfo,
    val language: LanguageCode
)