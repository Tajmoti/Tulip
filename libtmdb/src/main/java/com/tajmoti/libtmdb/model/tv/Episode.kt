package com.tajmoti.libtmdb.model.tv

import com.squareup.moshi.Json

data class Episode(
    @field:Json(name = "episode_number")
    val episodeNumber: Int,
    @field:Json(name = "season_number")
    val seasonNumber: Int,
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "overview")
    val overview: String?
)
