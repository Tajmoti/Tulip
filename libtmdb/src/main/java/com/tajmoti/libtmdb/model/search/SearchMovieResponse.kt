package com.tajmoti.libtmdb.model.search

import com.squareup.moshi.Json
import com.tajmoti.libtmdb.model.movie.Movie

data class SearchMovieResponse(
    @field:Json(name = "results")
    val results: List<Movie>,
)