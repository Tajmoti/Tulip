package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.StreamableKey

data class ItemPlayingProgressDto(
    val item: StreamableKey,
    val progress: Float
)