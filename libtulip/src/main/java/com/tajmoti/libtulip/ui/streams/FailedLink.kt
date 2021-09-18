package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtvprovider.VideoStreamRef

/**
 * Direct link loading failed for this stream.
 */
data class FailedLink(
    val stream: VideoStreamRef,
    val download: Boolean
)