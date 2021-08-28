package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtulip.model.hosted.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedTvDataRepository @Inject constructor(
    private val tvShowDao: TvShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val movieDao: MovieDao
) : HostedTvDataRepository {

    override fun getTvShowByKeyAsFlow(
        service: StreamingService,
        key: String
    ): Flow<HostedItem.TvShow?> {
        return tvShowDao.getByKeyAsFlow(service, key).map { it?.fromDb() }
    }

    override suspend fun getTvShowByKey(
        service: StreamingService,
        key: String
    ): HostedItem.TvShow? {
        return tvShowDao.getByKey(service, key)?.fromDb()
    }

    override suspend fun getTvShowsByTmdbId(id: TmdbItemId.Tv): List<HostedItem.TvShow> {
        return tvShowDao.getByTmdbId(id.id).map { it.fromDb() }
    }

    override suspend fun insertTvShow(show: HostedItem.TvShow) {
        tvShowDao.insert(show.toDb(show.info))
    }

    override suspend fun getSeasonsByTvShow(
        service: StreamingService,
        tvShowKey: String
    ): List<HostedSeason> {
        return seasonDao.getForShow(service, tvShowKey).map { it.fromDb() }
    }

    override suspend fun getSeasonByNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): HostedSeason? {
        return seasonDao.getBySeasonNumber(service, tvShowKey, seasonNumber)?.fromDb()
    }

    override suspend fun insertSeason(season: HostedSeason) {
        seasonDao.insert(season.toDb())
    }

    override suspend fun insertSeasons(seasons: List<HostedSeason>) {
        seasonDao.insert(seasons.map { it.toDb() })
    }

    override suspend fun getEpisodesBySeason(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): List<HostedEpisode> {
        return episodeDao.getForSeason(service, tvShowKey, seasonNumber).map { it.fromDb() }
    }

    override suspend fun getEpisodeByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        key: String
    ): HostedEpisode? {
        return episodeDao.getByKey(service, tvShowKey, seasonNumber, key)?.fromDb()
    }

    override suspend fun getEpisodeByTmdbIdentifiers(
        tmdbItemId: TmdbItemId.Tv,
        seasonNumber: Int,
        episodeNumber: Int
    ): List<HostedEpisode> {
        val shows = getTvShowsByTmdbId(tmdbItemId)
        return shows.mapNotNull { show ->
            episodeDao.getByNumber(show.service, show.info.key, seasonNumber, episodeNumber)
                ?.fromDb()
        }
    }

    override suspend fun insertEpisode(episode: HostedEpisode) {
        episodeDao.insert(episode.toDb())
    }

    override suspend fun insertEpisodes(episodes: List<HostedEpisode>) {
        episodeDao.insert(episodes.map { it.toDb() })
    }

    override suspend fun getMovieByKey(service: StreamingService, key: String): HostedItem.Movie? {
        return movieDao.getByKey(service, key)?.fromDb2()
    }

    override suspend fun getMovieByTmdbIdentifiers(tmdbItemId: TmdbItemId.Movie): List<HostedMovie> {
        return movieDao.getByTmdbId(tmdbItemId.id).map { it.fromDb() }
    }

    override suspend fun insertMovie(movie: HostedItem.Movie) {
        movieDao.insert(movie.toDb(movie.info))
    }
}