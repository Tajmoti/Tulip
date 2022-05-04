package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.EpisodeKey

data class SeasonEpisodeDto(
    val key: EpisodeKey,
    val episodeNumber: Int,
    val name: String?,
    val overview: String?,
    val stillPath: String? = null,
    val voteAverage: Float?
)