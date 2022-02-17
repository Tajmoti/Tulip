package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

interface HostedInfoDataSource {
    fun getTvShowByKey(key: TvShowKey.Hosted): Flow<TulipTvShowInfo.Hosted?>

    suspend fun insertTvShow(show: TulipTvShowInfo.Hosted)


    fun getMovieByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?>

    suspend fun insertMovie(movie: TulipMovie.Hosted)


    suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb)

    suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb)

    fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<List<TvShowKey.Hosted>>

    fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<List<MovieKey.Hosted>>
}
