package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.EpisodeKey

data class EpisodePlayingProgressDto(
    val item: EpisodeKey,
    val progress: Float
)