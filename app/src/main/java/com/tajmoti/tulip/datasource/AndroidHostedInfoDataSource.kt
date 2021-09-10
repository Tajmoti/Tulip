package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedMovie
import com.tajmoti.libtulip.model.hosted.HostedSeason
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import javax.inject.Inject

class AndroidHostedInfoDataSource @Inject constructor(
    private val tvShowDao: TvShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val movieDao: MovieDao
) : HostedInfoDataSource {

    override suspend fun getTvShowByKey(key: TvShowKey.Hosted): HostedItem.TvShow? {
        return tvShowDao.getByKey(key.streamingService, key.key)
            ?.fromDb()
    }

    override suspend fun getTvShowsByTmdbId(key: TvShowKey.Tmdb): List<HostedItem.TvShow> {
        return tvShowDao.getByTmdbId(key.id.id)
            .map { it.fromDb() }
    }

    override suspend fun insertTvShow(show: HostedItem.TvShow) {
        tvShowDao.insert(show.toDb(show.info))
    }


    override suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<HostedSeason> {
        return seasonDao.getForShow(key.streamingService, key.tvShowId)
            .map { it.fromDb() }
    }

    override suspend fun getSeasonByKey(key: SeasonKey.Hosted): HostedSeason? {
        return seasonDao.getBySeasonNumber(key.service, key.tvShowKey.key, key.seasonNumber)
            ?.fromDb()
    }

    override suspend fun insertSeasons(seasons: List<HostedSeason>) {
        seasonDao.insert(seasons.map { it.toDb() })
    }


    override suspend fun getEpisodesBySeason(key: SeasonKey.Hosted): List<HostedEpisode> {
        return episodeDao.getForSeason(key.service, key.tvShowKey.key, key.seasonNumber)
            .map { it.fromDb() }
    }

    override suspend fun getEpisodeByKey(key: EpisodeKey.Hosted): HostedEpisode? {
        val seasonKey = key.seasonKey
        val tvShowKey = seasonKey.tvShowKey
        return episodeDao.getByKey(key.service, tvShowKey.key, seasonKey.seasonNumber, key.key)
            ?.fromDb()
    }

    override suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): List<HostedEpisode> {
        val shows = getTvShowsByTmdbId(key.seasonKey.tvShowKey)
        val season = key.seasonKey
        return shows.mapNotNull { show ->
            episodeDao.getByNumber(
                show.service,
                show.info.key,
                season.seasonNumber,
                key.episodeNumber
            )
                ?.fromDb()
        }
    }

    override suspend fun insertEpisodes(episodes: List<HostedEpisode>) {
        episodeDao.insert(episodes.map { it.toDb() })
    }


    override suspend fun getMovieByKey(key: MovieKey.Hosted): HostedItem.Movie? {
        return movieDao.getByKey(key.streamingService, key.key)
            ?.fromDb2()
    }

    override suspend fun getMovieByTmdbKey(key: MovieKey.Tmdb): List<HostedMovie> {
        return movieDao.getByTmdbId(key.id.id)
            .map { it.fromDb() }
    }

    override suspend fun insertMovie(movie: HostedItem.Movie) {
        movieDao.insert(movie.toDb(movie.info))
    }
}