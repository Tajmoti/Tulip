package com.tajmoti.libtulip.data

import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv

interface LocalTvDataSource {
    suspend fun getTv(tvId: Long): Tv?

    suspend fun getSeason(tvId: Long, seasonNumber: Int): Season?

    suspend fun getSeasons(tvId: Long): List<Season>

    suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Episode?

    suspend fun getEpisodes(tvId: Long, seasonNumber: Int): List<Episode>

    suspend fun getMovie(movieId: Long): Movie?


    suspend fun insertTv(tv: Tv)

    suspend fun insertCompleteTv(tv: Tv, seasons: List<Season>)

    suspend fun insertSeason(tvId: Long, season: Season)

    suspend fun insertSeasons(tvId: Long, seasons: List<Season>)

    suspend fun insertEpisode(tvId: Long, episode: Episode)

    suspend fun insertEpisodes(tvId: Long, episodes: List<Episode>)

    suspend fun insertMovie(movie: Movie)
}