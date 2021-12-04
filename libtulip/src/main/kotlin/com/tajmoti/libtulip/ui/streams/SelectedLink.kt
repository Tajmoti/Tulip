package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtvprovider.VideoStreamRef

/**
 * Direct link extraction is not supported for the clicked streaming site.
 */
data class SelectedLink(
    val stream: VideoStreamRef.Resolved,
    val download: Boolean
)