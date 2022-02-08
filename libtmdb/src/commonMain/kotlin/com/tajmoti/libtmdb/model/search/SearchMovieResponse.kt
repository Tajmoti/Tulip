package com.tajmoti.libtmdb.model.search

import com.tajmoti.libtmdb.model.movie.Movie
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchMovieResponse(
    @SerialName("results")
    override val results: List<Movie>,
) : SearchResponse