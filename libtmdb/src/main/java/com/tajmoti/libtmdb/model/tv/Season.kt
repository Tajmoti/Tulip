package com.tajmoti.libtmdb.model.tv

import com.squareup.moshi.Json

data class Season(
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "overview")
    val overview: String?,
    @field:Json(name = "season_number")
    val seasonNumber: Int,
    @field:Json(name = "episodes")
    val episodes: List<Episode>
)
