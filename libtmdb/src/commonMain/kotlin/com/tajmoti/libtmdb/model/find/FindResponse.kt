package com.tajmoti.libtmdb.model.find

import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Tv
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FindResponse(
    @SerialName("movie_results")
    val movieResults: List<Movie>,
    @SerialName("tv_results")
    val tvResults: List<Tv>,
)
