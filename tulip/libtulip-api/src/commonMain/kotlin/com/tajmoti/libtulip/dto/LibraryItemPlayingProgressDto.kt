package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.StreamableKey

data class LibraryItemPlayingProgressDto(
    val key: StreamableKey,
    val progress: Float,
)