package com.tajmoti.libtmdb.model.tv

import com.squareup.moshi.Json
import com.tajmoti.libtmdb.model.FindResult

data class Tv(
    @field:Json(name = "id")
    override val id: Long,
    @field:Json(name = "name")
    override val name: String,
    @field:Json(name = "seasons")
    val seasons: List<SlimSeason>,
    @field:Json(name = "poster_path")
    override val posterPath: String?,
    @field:Json(name = "backdrop_path")
    override val backdropPath: String?
) : FindResult
