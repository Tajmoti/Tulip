package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey

interface LocalTvDataSource {
    suspend fun getTvShow(key: TvShowKey.Tmdb): TulipTvShowInfo.Tmdb?

    suspend fun getSeason(key: SeasonKey.Tmdb): TulipSeasonInfo.Tmdb?

    suspend fun getSeasons(key: TvShowKey.Tmdb): List<TulipSeasonInfo.Tmdb>

    suspend fun getEpisode(key: EpisodeKey.Tmdb): TulipEpisodeInfo.Tmdb?

    suspend fun getEpisodes(key: SeasonKey.Tmdb): List<TulipEpisodeInfo.Tmdb>

    suspend fun getMovie(key: MovieKey.Tmdb): TulipMovie.Tmdb?

    suspend fun insertTvShow(tv: TulipTvShowInfo.Tmdb)

    suspend fun insertMovie(movie: TulipMovie.Tmdb)
}