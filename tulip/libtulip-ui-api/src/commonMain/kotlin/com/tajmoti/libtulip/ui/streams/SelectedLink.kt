package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto

/**
 * Direct link extraction is not supported for the clicked streaming site.
 */
data class SelectedLink(
    val stream: StreamingSiteLinkDto,
    val download: Boolean
)