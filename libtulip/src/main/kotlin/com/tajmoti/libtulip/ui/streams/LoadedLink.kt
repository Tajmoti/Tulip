package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtvprovider.model.VideoStreamRef

data class LoadedLink(
    val stream: VideoStreamRef.Resolved,
    val directLink: String
)