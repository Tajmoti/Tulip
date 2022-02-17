package com.tajmoti.libtulip.data.impl

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryHostedInfoDataSource : HostedInfoDataSource {
    private val tvShows = MutableStateFlow(setOf<TulipTvShowInfo.Hosted>())
    private val movies = MutableStateFlow(setOf<TulipMovie.Hosted>())
    private val tmdbTvShowMappings = MutableStateFlow(mapOf<TvShowKey.Tmdb, Set<TvShowKey.Hosted>>())
    private val tmdbMovieMappings = MutableStateFlow(mapOf<MovieKey.Tmdb, Set<MovieKey.Hosted>>())

    override fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TulipTvShowInfo.Hosted?> {
        return tvShows.mapWithContext(LibraryDispatchers.libraryContext) { tvShows -> tvShows.firstOrNull { tvShow -> tvShow.key == key } }
    }

    override suspend fun insertTvShow(show: TulipTvShowInfo.Hosted) {
        tvShows.value = tvShows.value.plus(show)
    }

    override fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        return movies.mapWithContext(LibraryDispatchers.libraryContext) { movies -> movies.firstOrNull { movie -> movie.key == key } }
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movies.value = movies.value.plus(movie)
    }

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        val oldValue = tmdbTvShowMappings.value
        val oldSet = oldValue[tmdb] ?: mutableSetOf()
        val newSet = oldSet.plus(hosted)
        tmdbTvShowMappings.value = oldValue.plus(tmdb to newSet)
    }

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        val oldValue = tmdbMovieMappings.value
        val oldSet = oldValue[tmdb] ?: mutableSetOf()
        val newSet = oldSet.plus(hosted)
        tmdbMovieMappings.value = oldValue.plus(tmdb to newSet)
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<List<TvShowKey.Hosted>> {
        return tmdbTvShowMappings.mapWithContext(LibraryDispatchers.libraryContext) { keyMap ->
            keyMap.filterKeys { tmdbKey -> tmdbKey == tmdb }.flatMap { (_, hostedKeys) -> hostedKeys }
        }
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<List<MovieKey.Hosted>> {
        return tmdbMovieMappings.mapWithContext(LibraryDispatchers.libraryContext) { keyMap ->
            keyMap.filterKeys { tmdbKey -> tmdbKey == tmdb }.flatMap { (_, hostedKeys) -> hostedKeys }
        }
    }
}