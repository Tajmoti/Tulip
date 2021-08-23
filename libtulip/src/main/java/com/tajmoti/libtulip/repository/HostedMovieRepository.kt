package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedMovie
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId
import com.tajmoti.libtulip.model.key.MovieKey

interface HostedMovieRepository {

    suspend fun getMovieByKey(service: StreamingService, key: String): HostedItem.Movie?

    suspend fun getMovieByKey(key: MovieKey.Hosted): HostedItem.Movie? {
        return getMovieByKey(key.service, key.movieId)
    }

    suspend fun getMovieByTmdbIdentifiers(tmdbItemId: TmdbItemId.Movie): List<HostedMovie>

    suspend fun insertMovie(movie: HostedItem.Movie)
}