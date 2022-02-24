package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

class BrowserTvDataSource : LocalTvDataSource {
    private val tvStorage = BrowserStorage<TvShowKey.Tmdb, TulipTvShowInfo.Tmdb>("5")
    private val movieStorage = BrowserStorage<MovieKey.Tmdb, TulipMovie.Tmdb>("6")


    override fun getTvShow(key: TvShowKey.Tmdb): Flow<TulipTvShowInfo.Tmdb?> {
        return tvStorage.get(key)
    }

    override suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb) {
        tvStorage.put(tv.key, tv)
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?> {
        return movieStorage.get(key)
    }

    override suspend fun insertMovie(movie: TulipMovie.Tmdb) {
        movieStorage.put(movie.key, movie)
    }
}