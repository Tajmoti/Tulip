package com.tajmoti.libtmdb.model.movie

import com.squareup.moshi.Json
import com.tajmoti.libtmdb.model.FindResult

data class Movie(
    @field:Json(name = "id")
    override val id: Long,
    @field:Json(name = "title")
    val title: String,
    @field:Json(name = "overview")
    val overview: String?,
    @field:Json(name = "poster_path")
    override val posterPath: String?
) : FindResult {
    override val name: String
        get() = title
}
