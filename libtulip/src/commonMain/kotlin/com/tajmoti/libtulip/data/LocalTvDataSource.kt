package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

interface LocalTvDataSource {
    fun getTvShow(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?>

    fun getSeason(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?>

    fun getMovie(key: MovieKey.Tmdb): Flow<TulipMovie.Tmdb?>


    suspend fun insertTvShow(tv: TvShow.Tmdb)

    suspend fun insertSeason(season: SeasonWithEpisodes.Tmdb)

    suspend fun insertMovie(movie: TulipMovie.Tmdb)
}