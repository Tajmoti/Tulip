package com.tajmoti.libtmdb.model

import com.squareup.moshi.Json

data class SearchMovieResponse(
    @field:Json(name = "results")
    val results: List<MovieResult>,
)