package com.tajmoti.tulip.datasource

import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import javax.inject.Inject

class AndroidTvDataSource @Inject constructor(
    private val dao: TmdbDao
) : LocalTvDataSource {

    override suspend fun getTv(tvId: Long): Tv? {
        return dao.getTv(tvId)?.let { tv ->
            val seasons = getSeasons(tvId).map { season -> season.fromDb() }
            tv.fromDb(seasons)
        }
    }

    override suspend fun insertTv(tv: Tv) {
        dao.insertTv(tv.toDb())
    }

    override suspend fun insertCompleteTv(tv: Tv, seasons: List<Season>) {
        val episodes = seasons.flatMap { it.episodes }
        insertTv(tv)
        insertSeasons(tv.id, seasons)
        insertEpisodes(tv.id, episodes)
    }

    override suspend fun getSeason(tvId: Long, seasonNumber: Int): Season? {
        return dao.getSeason(tvId, seasonNumber)
            ?.let { getSeasonWithEpisodes(tvId, seasonNumber, it) }
    }

    private suspend fun getSeasonWithEpisodes(
        tvId: Long,
        seasonNumber: Int,
        dbSeason: DbTmdbSeason
    ): Season {
        val episodes = getEpisodes(tvId, seasonNumber)
        return dbSeason.fromDb(episodes)
    }

    override suspend fun getSeasons(tvId: Long): List<Season> {
        return dao.getSeasons(tvId).map { getSeasonWithEpisodes(tvId, it.seasonNumber, it) }
    }

    override suspend fun insertSeason(tvId: Long, season: Season) {
        val episodes = season.episodes.map { it.toDb(tvId) }
        dao.insertSeason(season.toDb(tvId))
        dao.insertEpisodes(episodes)
    }

    override suspend fun insertSeasons(tvId: Long, seasons: List<Season>) {
        val dbSeasons = seasons.map { season -> season.toDb(tvId) }
        dao.insertSeasons(dbSeasons)
    }

    override suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Episode? {
        return dao.getEpisode(tvId, seasonNumber, episodeNumber)?.fromDb()
    }

    override suspend fun getEpisodes(tvId: Long, seasonNumber: Int): List<Episode> {
        return dao.getEpisodes(tvId, seasonNumber).map { it.fromDb() }
    }

    override suspend fun insertEpisode(tvId: Long, episode: Episode) {
        dao.insertEpisode(episode.toDb(tvId))
    }

    override suspend fun insertEpisodes(tvId: Long, episodes: List<Episode>) {
        val dbEpisodes = episodes.map { it.toDb(tvId) }
        dao.insertEpisodes(dbEpisodes)
    }

    override suspend fun getMovie(movieId: Long): Movie? {
        return dao.getMovie(movieId)?.fromDb()
    }

    override suspend fun insertMovie(movie: Movie) {
        dao.insertMovie(movie.toDb())
    }
}