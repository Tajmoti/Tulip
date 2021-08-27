package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedSeason
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId
import com.tajmoti.libtulip.repository.HostedTvShowRepository
import com.tajmoti.libtvprovider.TvItemInfo
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import com.tajmoti.tulip.db.entity.hosted.DbEpisode
import com.tajmoti.tulip.db.entity.hosted.DbSeason
import com.tajmoti.tulip.db.entity.hosted.DbTvShow
import javax.inject.Inject

class AndroidHostedTvShowRepository @Inject constructor(
    private val tvShowDao: TvShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao
) : HostedTvShowRepository {
    override suspend fun getTvShowByKey(
        service: StreamingService,
        key: String
    ): HostedItem.TvShow? {
        val db = tvShowDao.getByKey(service, key) ?: return null
        return dbShowToLibShow(db)
    }

    private fun dbShowToLibShow(db: DbTvShow): HostedItem.TvShow {
        val info = TvItemInfo(db.key, db.name, db.language, db.firstAirDateYear)
        return HostedItem.TvShow(db.service, info, db.tmdbId?.let { TmdbItemId.Tv(it) })
    }

    override suspend fun getTvShowsByTmdbId(id: TmdbItemId.Tv): List<HostedItem.TvShow> {
        return tvShowDao.getByTmdbId(id.id)
            .map { dbShowToLibShow(it) }
    }

    override suspend fun insertTvShow(show: HostedItem.TvShow) {
        val info = show.info
        val db = DbTvShow(
            show.service,
            info.key,
            info.name,
            info.language,
            info.firstAirDateYear,
            show.tmdbId?.id
        )
        tvShowDao.insert(db)
    }

    override suspend fun getSeasonsByTvShow(
        service: StreamingService,
        tvShowKey: String
    ): List<HostedSeason> {
        return seasonDao.getForShow(service, tvShowKey)
            .map { dbSeasonToLib(it) }
    }

    private fun dbSeasonToLib(db: DbSeason): HostedSeason {
        return HostedSeason(db.service, db.tvShowKey, db.number)
    }

    override suspend fun getSeasonByNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): HostedSeason? {
        return seasonDao.getBySeasonNumber(service, tvShowKey, seasonNumber)
            ?.let { dbSeasonToLib(it) }
    }

    override suspend fun insertSeason(season: HostedSeason) {
        val db = libToDbSeason(season)
        seasonDao.insert(db)
    }

    override suspend fun insertSeasons(seasons: List<HostedSeason>) {
        val dbSeasons = seasons.map { libToDbSeason(it) }
        seasonDao.insert(dbSeasons)
    }

    private fun libToDbSeason(season: HostedSeason): DbSeason {
        return DbSeason(season.service, season.tvShowKey, season.number)
    }

    override suspend fun getEpisodesBySeason(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): List<HostedEpisode> {
        return episodeDao.getForSeason(service, tvShowKey, seasonNumber)
            .map(this::dbEpisodeToLib)
    }

    override suspend fun getEpisodeByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        key: String
    ): HostedEpisode? {
        val db = episodeDao.getByKey(service, tvShowKey, seasonNumber, key) ?: return null
        return dbEpisodeToLib(db)
    }

    private fun dbEpisodeToLib(db: DbEpisode) =
        HostedEpisode(db.service, db.tvShowKey, db.seasonNumber, db.key, db.number, db.name)

    override suspend fun getEpisodeByTmdbIdentifiers(
        tmdbItemId: TmdbItemId.Tv,
        seasonNumber: Int,
        episodeNumber: Int
    ): List<HostedEpisode> {
        val shows = getTvShowsByTmdbId(tmdbItemId)
        return getEpisodeByNumber(shows, seasonNumber, episodeNumber)
    }

    private suspend fun getEpisodeByNumber(
        shows: List<HostedItem.TvShow>,
        seasonNumber: Int,
        episodeNumber: Int
    ): List<HostedEpisode> {
        return shows.mapNotNull { show ->
            val tvShowKey = show.info.key
            val season = getSeasonByNumber(show.service, tvShowKey, seasonNumber)
                ?: return@mapNotNull null
            val episode =
                episodeDao.getByNumber(show.service, tvShowKey, season.number, episodeNumber)
                    ?: return@mapNotNull null
            dbEpisodeToLib(episode)
        }
    }

    override suspend fun insertEpisode(episode: HostedEpisode) {
        val db = libEpisodeToDb(episode)
        episodeDao.insert(db)
    }

    override suspend fun insertEpisodes(episodes: List<HostedEpisode>) {
        val dbEpisodes = episodes.map { libEpisodeToDb(it) }
        episodeDao.insert(dbEpisodes)
    }

    private fun libEpisodeToDb(episode: HostedEpisode): DbEpisode {
        return DbEpisode(
            episode.service,
            episode.tvShowKey,
            episode.seasonNumber,
            episode.key,
            episode.number,
            episode.name
        )
    }
}