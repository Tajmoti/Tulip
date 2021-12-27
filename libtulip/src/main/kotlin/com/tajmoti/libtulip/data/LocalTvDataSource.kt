package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

interface LocalTvDataSource {
    fun getTvShow(key: TvShowKey.Tmdb): Flow<TulipTvShowInfo.Tmdb?>

    fun getSeason(key: SeasonKey.Tmdb): Flow<TulipSeasonInfo.Tmdb?>

    fun getSeasons(key: TvShowKey.Tmdb): Flow<List<TulipSeasonInfo.Tmdb>>

    fun getEpisode(key: EpisodeKey.Tmdb): Flow<TulipEpisodeInfo.Tmdb?>

    fun getEpisodes(key: SeasonKey.Tmdb): Flow<List<TulipEpisodeInfo.Tmdb>>

    fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?>

    suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb)

    suspend fun insertMovie(movie: TulipMovie.Tmdb)
}