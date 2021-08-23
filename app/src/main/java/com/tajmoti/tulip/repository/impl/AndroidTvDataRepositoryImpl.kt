package com.tajmoti.tulip.repository.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.SlimSeason
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.repository.WritableTvDataRepository
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbEpisode
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbMovie
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbTv
import javax.inject.Inject

class AndroidTvDataRepositoryImpl @Inject constructor(
    private val dao: TmdbDao
) : WritableTvDataRepository {

    override suspend fun getTv(tvId: Long): Tv? {
        return dao.getTv(tvId)?.let {
            val seasons = getSeasons(tvId).map { SlimSeason(it.name, it.overview, it.seasonNumber) }
            Tv(it.id, it.name, seasons, it.posterPath)
        }
    }

    override suspend fun insertTv(tv: Tv) {
        logger.debug("Inserting '${tv.name}' (${tv.id})")
        dao.insertTv(DbTmdbTv(tv.id, tv.name, tv.posterPath))
    }

    override suspend fun insertCompleteTv(tv: Tv, seasons: List<Season>) {
        logger.debug("Inserting complete '${tv.name}' (${tv.id})")
        val episodes = seasons.flatMap { it.episodes }
        mapToAsyncJobs(
            { insertTv(tv) },
            { insertSeasons(tv.id, seasons) },
            { insertEpisodes(tv.id, episodes) }
        )
    }

    override suspend fun getSeason(tvId: Long, seasonNumber: Int): Season? {
        return dao.getSeason(tvId, seasonNumber)
            ?.let {
                val episodes = getEpisodes(tvId, seasonNumber)
                Season(it.name, it.overview, it.seasonNumber, episodes)
            }
    }

    override suspend fun getSeasons(tvId: Long): List<Season> {
        return dao.getSeasons(tvId)
            .map {
                val episodes = getEpisodes(tvId, it.seasonNumber)
                Season(it.name, it.overview, it.seasonNumber, episodes)
            }
    }

    override suspend fun insertSeason(tvId: Long, season: Season) {
        logger.debug("Inserting season of $tvId - ${season.seasonNumber}")
        val episodes = season.episodes.map { toDbEpisode(tvId, it) }
        mapToAsyncJobs(
            { dao.insertSeason(libSeasonToDb(tvId, season)) },
            { dao.insertEpisodes(episodes) }
        )
    }

    override suspend fun insertSeasons(tvId: Long, seasons: List<Season>) {
        logger.debug("Inserting seasons of $tvId - ${seasons.size} seasons")
        val dbSeasons = seasons.map { season -> libSeasonToDb(tvId, season) }
        dao.insertSeasons(dbSeasons)
    }

    private fun libSeasonToDb(tvId: Long, season: Season): DbTmdbSeason {
        return DbTmdbSeason(tvId, season.name, season.overview, season.seasonNumber)
    }

    override suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Episode? {
        return dao.getEpisode(tvId, seasonNumber, episodeNumber)
            ?.let { dbEpisodeToLib(it) }
    }

    override suspend fun getEpisodes(tvId: Long, seasonNumber: Int): List<Episode> {
        return dao.getEpisodes(tvId, seasonNumber)
            .map { dbEpisodeToLib(it) }
    }

    private fun dbEpisodeToLib(it: DbTmdbEpisode) =
        Episode(it.episodeNumber, it.seasonNumber, it.name, it.overview)

    override suspend fun insertEpisode(tvId: Long, episode: Episode) {
        logger.debug("Inserting episode '${episode.name}' ${episode.episodeNumber} of $tvId")
        dao.insertEpisode(toDbEpisode(tvId, episode))
    }

    override suspend fun insertEpisodes(tvId: Long, episodes: List<Episode>) {
        logger.debug("Inserting episodes of $tvId - ${episodes.size} episodes")
        val dbEpisodes = episodes.map { toDbEpisode(tvId, it) }
        dao.insertEpisodes(dbEpisodes)
    }

    private fun toDbEpisode(tvId: Long, episode: Episode): DbTmdbEpisode {
        return DbTmdbEpisode(
            tvId,
            episode.seasonNumber,
            episode.episodeNumber,
            episode.name,
            episode.overview
        )
    }

    override suspend fun getMovie(movieId: Long): Movie? {
        return dao.getMovie(movieId)
            ?.let { Movie(it.id, it.name, it.overview, it.posterPath) }
    }

    override suspend fun insertMovie(movie: Movie) {
        dao.insertMovie(DbTmdbMovie(movie.id, movie.name, movie.overview, movie.posterPath))
    }
}