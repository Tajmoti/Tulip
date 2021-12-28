package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

interface LocalTvDataSource {
    fun getTvShow(key: TvShowKey.Tmdb): Flow<TulipTvShowInfo.Tmdb?>

    suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb)


    fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?>

    suspend fun insertMovie(movie: TulipMovie.Tmdb)
}