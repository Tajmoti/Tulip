package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtulip.model.*
import com.tajmoti.libtulip.repository.TvShowRepository
import com.tajmoti.tulip.db.dao.EpisodeDao
import com.tajmoti.tulip.db.dao.SeasonDao
import com.tajmoti.tulip.db.dao.TvShowDao
import com.tajmoti.tulip.db.entity.DbEpisode
import com.tajmoti.tulip.db.entity.DbSeason
import com.tajmoti.tulip.db.entity.DbTvShow
import javax.inject.Inject

class AndroidTvShowRepository @Inject constructor(
    private val tvShowDao: TvShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao
) : TvShowRepository {
    override suspend fun getTvShowByKey(service: StreamingService, key: String): TulipTvShow? {
        val db = tvShowDao.getByKey(service, key) ?: return null
        return TulipTvShow(
            db.service,
            db.key,
            db.name,
            db.language,
            db.firstAirDateYear,
            db.tmdbId?.let { TmdbId(it) })
    }

    override suspend fun insertTvShow(show: TulipTvShow) {
        val db = DbTvShow(
            show.service,
            show.key,
            show.name,
            show.language,
            show.firstAirDateYear,
            show.tmdbId?.id
        )
        tvShowDao.insert(db)
    }

    override suspend fun getSeasonByKey(
        service: StreamingService,
        tvShowKey: String,
        key: String
    ): TulipSeason? {
        val db = seasonDao.getForShow(service, tvShowKey, key) ?: return null
        return TulipSeason(db.service, db.tvShowKey, db.key, db.number)
    }

    override suspend fun insertSeason(season: TulipSeason) {
        val db = DbSeason(season.service, season.tvShowKey, season.key, season.number)
        seasonDao.insert(db)
    }

    override suspend fun getEpisodesBySeason(
        service: StreamingService,
        tvShowKey: String,
        seasonKey: String
    ): List<TulipEpisode> {
        return episodeDao.getForSeason(service, tvShowKey, seasonKey)
            .map {
                TulipEpisode(
                    it.service,
                    it.tvShowKey,
                    it.seasonKey,
                    it.key,
                    it.number,
                    it.name
                )
            }
    }

    override suspend fun getEpisodesByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonKey: String,
        key: String
    ): TulipEpisode? {
        val db = episodeDao.getByKey(service, tvShowKey, seasonKey, key) ?: return null
        return TulipEpisode(db.service, db.tvShowKey, db.seasonKey, db.key, db.number, db.name)
    }

    override suspend fun insertEpisode(episode: TulipEpisode) {
        val db = DbEpisode(
            episode.service,
            episode.tvShowKey,
            episode.seasonKey,
            episode.key,
            episode.number,
            episode.name
        )
        episodeDao.insert(db)
    }
}