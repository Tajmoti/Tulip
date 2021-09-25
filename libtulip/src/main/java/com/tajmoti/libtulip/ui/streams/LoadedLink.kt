package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtvprovider.VideoStreamRef

data class LoadedLink(
    val stream: VideoStreamRef.Resolved,
    val directLink: String
)