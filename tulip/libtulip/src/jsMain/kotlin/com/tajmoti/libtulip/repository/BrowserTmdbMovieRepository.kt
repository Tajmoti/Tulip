package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import kotlinx.coroutines.flow.Flow

class BrowserTmdbMovieRepository : TmdbMovieRepository {
    private val movieStorage = BrowserStorage<MovieKey.Tmdb, TulipMovie.Tmdb>()

    override fun findByKey(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return movieStorage.get(key)
    }

    override suspend fun insert(repo: TulipMovie.Tmdb) {
        movieStorage.put(repo.key, repo)
    }
}