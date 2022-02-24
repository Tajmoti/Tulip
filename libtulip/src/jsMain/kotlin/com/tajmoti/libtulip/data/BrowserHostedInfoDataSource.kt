package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserHostedInfoDataSource : HostedInfoDataSource {
    private val tvShows = BrowserStorage<TvShowKey.Hosted, TulipTvShowInfo.Hosted>()
    private val movies = BrowserStorage<MovieKey.Hosted, TulipMovie.Hosted>()
    private val tmdbTvShowMappings = BrowserStorage<TvShowKey.Tmdb, Set<TvShowKey.Hosted>>()
    private val tmdbMovieMappings = BrowserStorage<MovieKey.Tmdb, Set<MovieKey.Hosted>>()

    override fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TulipTvShowInfo.Hosted?> {
        return tvShows.get(key)
    }

    override suspend fun insertTvShow(show: TulipTvShowInfo.Hosted) {
        tvShows.put(show.key, show)
    }

    override fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        return movies.get(key)
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movies.put(movie.key, movie)
    }

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        tmdbTvShowMappings.update(tmdb) { oldValue -> (oldValue ?: mutableSetOf()).plus(hosted) }
    }

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        tmdbMovieMappings.update(tmdb) { oldValue -> (oldValue ?: mutableSetOf()).plus(hosted) }
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return tmdbTvShowMappings.get(tmdb).map { it ?: emptySet() }
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return tmdbMovieMappings.get(tmdb).map { it ?: emptySet() }
    }
}