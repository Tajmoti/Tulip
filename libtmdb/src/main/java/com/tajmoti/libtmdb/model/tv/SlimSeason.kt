package com.tajmoti.libtmdb.model.tv

import com.squareup.moshi.Json

data class SlimSeason(
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "overview")
    val overview: String?,
    @field:Json(name = "season_number")
    val seasonNumber: Int
)
