package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtulip.model.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.repository.MovieRepository
import com.tajmoti.libtulip.repository.TvShowRepository
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TvDataServiceImpl @Inject constructor(
    private val tvShowRepo: TvShowRepository,
    private val movieRepo: MovieRepository,
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val tmdbService: TmdbService
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

    override suspend fun searchAndSaveItems(query: String): Result<List<TulipSearchResult>> {
        logger.debug("Searching '{}'", query)
        val searchResult = tvProvider.search(query)
        if (searchResult.none { it.value.isSuccess }) {
            logger.warn("No successful results!")
            return Result.failure(NoSuccessfulResultsException)
        }
        logExceptions(searchResult)
        val successfulItems = flatMapSuccessfulResults(searchResult)
        logger.debug("Found {} results", successfulItems.size)
        val recognizedItems = zipWithTmdbIds(successfulItems)
        insertSearchResultsToDb(recognizedItems)
        return Result.success(recognizedItems)
    }

    private suspend fun insertSearchResultsToDb(result: List<TulipSearchResult>) {
        // TODO Atomicity
        for (item in result) {
            insertTvItemIntoDb(item)
        }
    }

    private suspend fun insertTvItemIntoDb(item: TulipSearchResult) {
        when (item) {
            is TulipTvShow -> tvShowRepo.insertTvShow(item)
            is TulipMovie -> movieRepo.insertMovie(item)
        }
    }

    private suspend fun zipWithTmdbIds(
        items: List<Pair<StreamingService, TvItem>>
    ): List<TulipSearchResult> {
        val results = fetchTmdbIdsParallel(items.map { it.second })
        return items.zip(results) { s2i, tmdbId ->
            val (service, item) = s2i
            when (item) {
                is TvItem.Show -> TulipTvShow(service, item, tmdbId)
                is TvItem.Movie -> TulipMovie(service, item, tmdbId)
            }
        }
    }

    private suspend fun fetchTmdbIdsParallel(items: List<TvItem>): List<TmdbId?> {
        val jobs = coroutineScope {
            items.map { async { findTmdbId(it) } }
        }
        return awaitAll(*jobs.toTypedArray())
    }

    private suspend fun findTmdbId(item: TvItem): TmdbId? {
        val foundId = try {
            when (item) {
                is TvItem.Show -> tmdbService.searchTv(
                    item.name,
                    item.firstAirDateYear
                ).results.firstOrNull()?.id
                is TvItem.Movie -> tmdbService.searchMovie(
                    item.name,
                    item.firstAirDateYear
                ).results.firstOrNull()?.id
            }
        } catch (e: Throwable) {
            logger.warn("Exception while searching IMDB id for {}", item, e)
            null
        }
        return foundId?.let { TmdbId(it) }
    }

    private fun flatMapSuccessfulResults(
        results: Map<StreamingService, Result<List<TvItem>>>
    ): List<Pair<StreamingService, TvItem>> {
        return results
            .mapNotNull { (service, itemListResult) ->
                val itemList = itemListResult
                    .getOrElse { return@mapNotNull null }
                service to itemList
            }
            .flatMap { (service, itemListResult) ->
                itemListResult.map { service to it }
            }
    }

    private fun logExceptions(searchResult: Map<StreamingService, Result<List<TvItem>>>) {
        for ((service, result) in searchResult) {
            val exception = result.exceptionOrNull() ?: continue
            logger.warn("{} failed with", service, exception)
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