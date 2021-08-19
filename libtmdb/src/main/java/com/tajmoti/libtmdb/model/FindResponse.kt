package com.tajmoti.libtmdb.model

import com.squareup.moshi.Json

data class FindResponse(
    @field:Json(name = "movie_results")
    val movieResults: List<MovieResult>,
    @field:Json(name = "tv_results")
    val tvResults: List<TvResult>,
)
