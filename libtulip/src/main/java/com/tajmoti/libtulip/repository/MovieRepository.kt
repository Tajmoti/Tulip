package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey

interface MovieRepository {

    suspend fun getMovieByKey(service: StreamingService, key: String): TulipMovie?

    suspend fun getMovieByKey(key: MovieKey): TulipMovie? {
        return getMovieByKey(key.service, key.movieId)
    }

    suspend fun insertMovie(movie: TulipMovie)
}