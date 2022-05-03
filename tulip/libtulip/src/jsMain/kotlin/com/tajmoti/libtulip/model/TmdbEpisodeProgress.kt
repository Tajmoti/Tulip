package com.tajmoti.libtulip.model

import com.tajmoti.libtulip.model.key.EpisodeKey
import kotlinx.serialization.Serializable

@Serializable
data class TmdbEpisodeProgress(
    val key: EpisodeKey.Tmdb,
    val progress: Float
)

