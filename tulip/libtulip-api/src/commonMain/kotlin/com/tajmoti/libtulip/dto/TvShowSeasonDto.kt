package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.SeasonKey

data class TvShowSeasonDto(
    val key: SeasonKey,
    val seasonNumber: Int
)