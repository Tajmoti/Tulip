package com.tajmoti.libtulip.dto

data class TvShowDto(
    val name: String,
    /**
     * ISO 639-1 language codes, may be empty.
     */
    val languages: List<String>,
    val firstAirDateYear: Int?,
    val backdropPath: String?,
    val seasons: List<TvShowSeasonDto>,
    val isFavorite: Boolean,
)