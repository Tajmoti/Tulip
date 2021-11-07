package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtvprovider.VideoStreamRef

data class UnloadedVideoStreamRef(
    val info: VideoStreamRef,
    val linkExtractionSupported: Boolean,
    val language: LanguageCode
)