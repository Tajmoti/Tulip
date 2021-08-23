package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtvprovider.VideoStreamRef

data class UnloadedVideoStreamRef(
    val info: VideoStreamRef,
    val linkExtractionSupported: Boolean
)