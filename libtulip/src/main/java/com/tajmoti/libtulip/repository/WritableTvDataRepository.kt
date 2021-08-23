package com.tajmoti.libtulip.repository

import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv

interface WritableTvDataRepository : ReadOnlyTvDataRepository {
    suspend fun insertTv(tv: Tv)

    suspend fun insertCompleteTv(tv: Tv, seasons: List<Season>)

    suspend fun insertSeason(tvId: Long, season: Season)

    suspend fun insertSeasons(tvId: Long, seasons: List<Season>)

    suspend fun insertEpisode(tvId: Long, episode: Episode)

    suspend fun insertEpisodes(tvId: Long, episodes: List<Episode>)

    suspend fun insertMovie(movie: Movie)
}