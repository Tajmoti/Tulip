package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import kotlinx.coroutines.flow.Flow

class BrowserHostedMovieRepository : HostedMovieRepository {
    private val movies = BrowserStorage<MovieKey.Hosted, TulipMovie.Hosted>()

    override fun findByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        return movies.get(key)
    }

    override suspend fun insert(repo: TulipMovie.Hosted) {
        movies.put(repo.key, repo)
    }
}