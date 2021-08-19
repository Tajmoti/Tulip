package com.tajmoti.libtulip.model

import com.tajmoti.libtvprovider.VideoStreamRef

data class UnloadedVideoStreamRef(
    val info: VideoStreamRef,
    val linkExtractionSupported: Boolean
)