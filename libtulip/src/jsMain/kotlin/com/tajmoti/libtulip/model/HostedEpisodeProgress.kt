package com.tajmoti.libtulip.model

import com.tajmoti.libtulip.model.key.EpisodeKey
import kotlinx.serialization.Serializable

@Serializable
data class HostedEpisodeProgress(
    val key: EpisodeKey.Hosted,
    val progress: Float
)