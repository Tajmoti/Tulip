package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.MovieKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserMovieMappingRepository : MovieMappingRepository {
    private val tmdbMovieMappings = BrowserStorage<MovieKey.Tmdb, Set<MovieKey.Hosted>>()

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        tmdbMovieMappings.update(tmdb) { oldValue -> (oldValue ?: mutableSetOf()).plus(hosted) }
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return tmdbMovieMappings.get(tmdb).map { it ?: emptySet() }
    }
}