package com.tajmoti.libtmdb.model

import com.squareup.moshi.Json

data class TvResult(
    @field:Json(name = "id")
    override val id: Long,
    @field:Json(name = "name")
    override val name: String,
    @field:Json(name = "poster_path")
    override val posterPath: String?
) : FindResult
