package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.repository.MovieRepository
import com.tajmoti.libtulip.repository.TvShowRepository
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.db.MissingEntityException
import com.tajmoti.tulip.model.NoSuccessfulResultsException
import javax.inject.Inject

class TvDataServiceImpl @Inject constructor(
    private val tvShowRepo: TvShowRepository,
    private val movieRepo: MovieRepository,
    private val tvProvider: MultiTvProvider<StreamingService>
) : TvDataService {

    override suspend fun getTvShow(key: TvShowKey): Result<TvItem.Show> {
        logger.debug("Retrieving {}", key)
        val dbInfo = tvShowRepo.getTvShowByKey(key)
        if (dbInfo == null) {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        return tvProvider.getShow(key.service, dbInfo.apiInfo)
    }

    override suspend fun getSeason(key: SeasonKey): Result<Season> {
        logger.debug("Retrieving {}", key)
        val dbSeason = tvShowRepo.getSeasonByKey(key)
        if (dbSeason == null) {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        val dbEpisodes = tvShowRepo.getEpisodesBySeason(key)
        val info = dbSeason.toApiInfo(dbEpisodes)
        return tvProvider.getSeason(key.service, info)
    }

    override suspend fun getStreamable(key: StreamableKey): Result<StreamableInfo> {
        logger.debug("Retrieving {}", key)
        return when (key) {
            is EpisodeKey -> getEpisodeByKey(key)
            is MovieKey -> getMovieByKey(key)
        }
    }

    private suspend fun getMovieByKey(key: MovieKey): Result<StreamableInfo.Movie> {
        logger.debug("Retrieving {}", key)
        val dbMovie = movieRepo.getMovieByKey(key)
            ?: run {
                logger.warn("Movie {} not found", key)
                return Result.failure(MissingEntityException)
            }
        return tvProvider.getMovie(key.service, dbMovie.apiInfo)
            .map { StreamableInfo.Movie(it) }
    }

    private suspend fun getEpisodeByKey(key: EpisodeKey): Result<StreamableInfo.TvShow> {
        logger.debug("Retrieving {}", key)
        val dbShow = tvShowRepo.getTvShowByKey(key.service, key.tvShowId)
            ?: run {
                logger.warn("{} not found", key)
                return Result.failure(MissingEntityException)
            }
        val dbSeason = tvShowRepo.getSeasonByKey(key.service, key.tvShowId, key.seasonId)
            ?: run {
                logger.warn("{} not found", key)
                return Result.failure(MissingEntityException)
            }
        val dbEpisode =
            tvShowRepo.getEpisodesByKey(key.service, key.tvShowId, key.seasonId, key.episodeId)
                ?: run {
                    logger.warn("{} not found", key)
                    return Result.failure(MissingEntityException)
                }
        return tvProvider.getEpisode(key.service, dbEpisode.apiInfo)
            .map { StreamableInfo.TvShow(dbShow, dbSeason, it) }
    }

    override suspend fun searchAndSaveItems(query: String): Result<List<Pair<StreamingService, TvItem>>> {
        logger.debug("Searching '{}'", query)
        val searchResult = tvProvider.search(query)
        for ((service, result) in searchResult) {
            val successfulResult = result.getOrNull() ?: continue
            insertSearchResultsToDb(service, successfulResult)
        }
        for ((service, result) in searchResult) {
            val exception = result.exceptionOrNull() ?: continue
            logger.warn("{} failed with", service, exception)
        }
        if (searchResult.none { it.second.isSuccess }) {
            logger.warn("No successful results!")
            return Result.failure(NoSuccessfulResultsException)
        }
        val successfulItems = searchResult
            .mapNotNull {
                val res = it.second.getOrNull() ?: return@mapNotNull null
                it.first to res
            }
            .flatMap { a -> a.second.map { a.first to it } }
        logger.debug("Found {} results", successfulItems.size)
        return Result.success(successfulItems)
    }

    private suspend fun insertSearchResultsToDb(service: StreamingService, result: List<TvItem>) {
        // TODO Atomicity
        for (item in result) {
            insertTvItemIntoDb(item, service)
        }
    }

    private suspend fun insertTvItemIntoDb(item: TvItem, service: StreamingService) {
        when (item) {
            is TvItem.Show -> {
                val dbItem = TulipTvShow(service, item)
                tvShowRepo.insertTvShow(dbItem)
            }
            is TvItem.Movie -> {
                val dbItem = TulipMovie(service, item)
                movieRepo.insertMovie(dbItem)
            }
        }
    }

    override suspend fun fetchAndSaveSeasons(
        key: TvShowKey,
        show: TvItem.Show
    ): Result<List<Season>> {
        val result = show.fetchSeasons()
            .getOrElse {
                logger.warn("Failed to fetch seasons for {}", show)
                return Result.failure(it)
            }
        logger.debug("Saving {} seasons of {} {}", result.size, key, show)
        insertSeasonsToDb(key, result)
        return Result.success(result)
    }

    private suspend fun insertSeasonsToDb(key: TvShowKey, result: List<Season>) {
        // TODO Atomicity
        for (season in result) {
            insertSeasonIntoDb(key, season)
        }
    }

    private suspend fun insertSeasonIntoDb(key: TvShowKey, season: Season) {
        tvShowRepo.insertSeason(TulipSeason(key.service, key.tvShowId, season))
        for (episode in season.episodes) {
            insertEpisodeIntoDb(key, season, episode)
        }
    }

    private suspend fun insertEpisodeIntoDb(key: TvShowKey, season: Season, episode: Episode) {
        val dbEpisode = TulipEpisode(key.service, key.tvShowId, season.key, episode)
        tvShowRepo.insertEpisode(dbEpisode)
    }
}