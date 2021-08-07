package com.tajmoti.tulip.ui.streams

import com.tajmoti.libtvprovider.VideoStreamRef

data class UnloadedVideoStreamRef(
    val info: VideoStreamRef,
    val linkExtractionSupported: Boolean
)