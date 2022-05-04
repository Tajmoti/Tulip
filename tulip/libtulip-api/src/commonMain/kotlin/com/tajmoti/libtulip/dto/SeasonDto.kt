package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.SeasonKey

data class SeasonDto(
    val key: SeasonKey,
    val seasonNumber: Int,
    val episodes: List<SeasonEpisodeDto>
)