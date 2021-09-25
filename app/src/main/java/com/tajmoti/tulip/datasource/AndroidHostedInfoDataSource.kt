package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
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

    override suspend fun getTvShowByKey(key: TvShowKey.Hosted): TulipTvShowInfo.Hosted? {
        return tvShowDao.getByKey(key.streamingService, key.id)
            ?.fromDb(key, getSeasonsByTvShow(key))
    }

    override suspend fun getTvShowsByTmdbId(key: TvShowKey.Tmdb): List<TulipTvShowInfo.Hosted> {
        return tvShowDao.getByTmdbId(key.id.id)
            .map {
                val tvShowKey = TvShowKey.Hosted(it.service, it.key)
                it.fromDb(tvShowKey, getSeasonsByTvShow(tvShowKey))
            }
    }

    override suspend fun insertTvShow(show: TulipTvShowInfo.Hosted) {
        tvShowDao.insert(show.toDb(show.info))
        insertSeasons(show.seasons)
    }


    override suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<TulipSeasonInfo.Hosted> {
        return seasonDao.getForShow(key.streamingService, key.id)
            .map {
                val seasonKey = SeasonKey.Hosted(key, it.number)
                it.fromDb(key, getEpisodesBySeason(seasonKey))
            }
    }

    override suspend fun getSeasonByKey(key: SeasonKey.Hosted): TulipSeasonInfo.Hosted? {
        return seasonDao.getBySeasonNumber(key.streamingService, key.tvShowKey.id, key.seasonNumber)
            ?.fromDb(key.tvShowKey, getEpisodesBySeason(key))
    }

    private suspend inline fun insertSeasons(seasons: List<TulipSeasonInfo.Hosted>) {
        seasonDao.insert(seasons.map { it.toDb() })
        insertEpisodes(seasons.flatMap { it.episodes })
    }


    override suspend fun getEpisodesBySeason(key: SeasonKey.Hosted): List<TulipEpisodeInfo.Hosted> {
        return episodeDao.getForSeason(key.streamingService, key.tvShowKey.id, key.seasonNumber)
            .map { it.fromDb(key) }
    }

    override suspend fun getEpisodeByKey(key: EpisodeKey.Hosted): TulipEpisodeInfo.Hosted? {
        return episodeDao.getByKey(key.streamingService, key.tvShowKey.id, key.seasonNumber, key.id)
            ?.fromDb()
    }

    override suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): List<TulipEpisodeInfo.Hosted> {
        val shows = getTvShowsByTmdbId(key.tvShowKey)
        return shows.mapNotNull { show ->
            episodeDao.getByNumber(
                show.key.streamingService,
                show.info.id,
                key.seasonNumber,
                key.episodeNumber
            )
                ?.fromDb()
        }
    }

    private suspend inline fun insertEpisodes(episodes: List<TulipEpisodeInfo.Hosted>) {
        episodeDao.insert(episodes.map { it.toDb() })
    }


    override suspend fun getMovieByKey(key: MovieKey.Hosted): TulipMovie.Hosted? {
        return movieDao.getByKey(key.streamingService, key.id)
            ?.fromDb(key)
    }

    override suspend fun getMovieByTmdbKey(key: MovieKey.Tmdb): List<TulipMovie.Hosted> {
        return movieDao.getByTmdbId(key.id.id)
            .map { it.fromDb() }
    }

    override suspend fun insertMovie(movie: TulipMovie.Hosted) {
        movieDao.insert(movie.toDb(movie.info))
    }
}