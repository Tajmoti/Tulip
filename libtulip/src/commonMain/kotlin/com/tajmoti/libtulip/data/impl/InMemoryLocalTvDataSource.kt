package com.tajmoti.libtulip.data.impl

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryLocalTvDataSource : LocalTvDataSource {
    private val tvShows = MutableStateFlow<Set<TulipTvShowInfo.Tmdb>>(emptySet())
    private val movies = MutableStateFlow<Set<TulipMovie.Tmdb>>(emptySet())

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TulipTvShowInfo.Tmdb?> {
        return tvShows.mapWithContext(LibraryDispatchers.libraryContext) { it.firstOrNull { tvShow -> tvShow.key == key } }
    }

    override suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb) {
        tvShows.value = tvShows.value.plus(tv)
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return movies.mapWithContext(LibraryDispatchers.libraryContext) { movies -> movies.firstOrNull { movie -> movie.key == key } }
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        movies.value = movies.value.plus(movie)
    }
}