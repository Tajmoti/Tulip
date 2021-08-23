package com.tajmoti.libtmdb.model.find

import com.squareup.moshi.Json
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Tv

data class FindResponse(
    @field:Json(name = "movie_results")
    val movieResults: List<Movie>,
    @field:Json(name = "tv_results")
    val tvResults: List<Tv>,
)
