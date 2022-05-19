package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto

data class LoadedLink(
    val stream: StreamingSiteLinkDto,
    val directLink: String
)