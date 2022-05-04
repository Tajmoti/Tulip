package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtvprovider.model.VideoStreamRef

data class UnloadedVideoStreamRef(
    val info: VideoStreamRef,
    val linkExtractionSupported: Boolean,
    val language: LanguageCode
) : IdentityItem<VideoStreamRef> {
    override val key = info
}