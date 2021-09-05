package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.awaitAll
import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.parallelMap
import com.tajmoti.commonutils.parallelMapBoth
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.hosted.*
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class HostedTvDataServiceImpl @Inject constructor(
    private val hostedTvDataRepo: HostedTvDataRepository,
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val tmdbService: TvDataService
) : HostedTvDataService {

    override suspend fun search(query: String): Result<Map<StreamingService, Result<List<SearchResult>>>> {
        logger.debug("Searching '{}'", query)
        val searchResult = tvProvider.search(query)
        if (searchResult.none { it.value.isSuccess }) {
            logger.warn("No successful results!")
            return Result.failure(NoSuccessfulResultsException)
        }
        logExceptions(searchResult)
        return Result.success(searchResult)
    }

    override suspend fun getTvShow(key: TvShowKey.Hosted): Result<TvShowInfo> {
        logger.debug("Retrieving {}", key)
        val result = tvProvider.getShow(key.streamingService, key.tvShowId)
            .onFailure { logger.warn("Failed to retrieve TV Show $key", it) }
            .onSuccess { insertTvShowToDb(key, it) }
            .getOrElse { getShowFromDb(key).getOrNull() }
            ?: return Result.failure(MissingEntityException) // TODO Better handling here
        return Result.success(result)
    }

    private suspend fun getShowFromDb(key: TvShowKey.Hosted): Result<TvShowInfo> {
        val show = hostedTvDataRepo.getTvShowByKey(key)
            ?: return Result.failure(MissingEntityException)
        val seasons = hostedTvDataRepo.getSeasonsByTvShow(key)
        val episodes = seasons.parallelMapBoth {
            val hostedSeasonKey = SeasonKey.Hosted(key, it.number)
            hostedTvDataRepo.getEpisodesBySeason(hostedSeasonKey)
        }
        val seasonsToReturns = episodes
            .map { season ->
                val seasonEpisodes = season.second.map { ep ->
                    EpisodeInfo(ep.key, ep.number, ep.name)
                }
                Season(season.first.tvShowKey, season.first.number, seasonEpisodes)
            }
        val tvInfo = TvItemInfo(key.tvShowId, show.name, show.language, show.firstAirDateYear)
        val info = TvShowInfo(key.tvShowId, tvInfo, seasonsToReturns)
        return Result.success(info)
    }

    override suspend fun getSeason(key: SeasonKey.Hosted): Result<Season> {
        logger.debug("Retrieving {}", key)
        return tvProvider.getShow(key.service, key.tvShowId)
            .map { it.seasons.first { season -> season.number == key.seasonNumber } }
    }

    override suspend fun getSeasons(key: TvShowKey.Hosted): Result<List<Season>> {
        logger.debug("Retrieving {}", key)
        return tvProvider.getShow(key.streamingService, key.tvShowId)
            .map { it.seasons }
    }

    override suspend fun getStreamableInfo(key: StreamableKey.Hosted): Result<StreamableInfoWithLanguage> {
        return when (key) {
            is EpisodeKey.Hosted -> getEpisodeInfo(key)
            is MovieKey.Hosted -> getMovieInfo(key)
        }
    }

    private suspend fun getEpisodeInfo(key: EpisodeKey.Hosted): Result<StreamableInfoWithLanguage> {
        val abc = coroutineScope {
            val epJob = async { hostedTvDataRepo.getEpisodeByKey(key) }
            val ssJob = async { hostedTvDataRepo.getSeasonByKey(key.seasonKey) }
            val shJob = async { hostedTvDataRepo.getTvShowByKey(key.seasonKey.tvShowKey) }
            Triple(epJob, ssJob, shJob)
        }
        val (eps, sss, shs) = abc.awaitAll()
        val ep = eps ?: run {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        val ss = sss ?: run {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        val sh = shs ?: run {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        val info = StreamableInfo.Episode(
            showName = sh.name,
            seasonNumber = ss.number,
            info = TulipEpisodeInfo(ep.number, ep.name)
        )
        return Result.success(StreamableInfoWithLanguage(info, shs.language))
    }

    private suspend fun getMovieInfo(key: MovieKey.Hosted): Result<StreamableInfoWithLanguage> {
        val movie = hostedTvDataRepo.getMovieByKey(key) ?: run {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        val info = StreamableInfo.Movie(movie.name)
        return Result.success(StreamableInfoWithLanguage(info, movie.language))
    }

    private fun logExceptions(searchResult: Map<StreamingService, Result<List<SearchResult>>>) {
        for ((service, result) in searchResult) {
            val exception = result.exceptionOrNull() ?: continue
            logger.warn("{} failed with", service, exception)
        }
    }

    override suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Result<List<HostedEpisode>> {
        logger.debug("Retrieving episode by $key")
        prefetchTvShowByTmdbId(key.seasonKey.tvShowKey)
        val season = key.seasonKey
        val episodes = hostedTvDataRepo.getEpisodeByTmdbIdentifiers(
            season.tvShowKey.id,
            season.seasonNumber,
            key.episodeNumber
        )
        logger.debug("Episode retrieved by $key")
        return Result.success(episodes)
    }

    override suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<HostedMovie>> {
        val movies = hostedTvDataRepo.getMovieByTmdbIdentifiers(key.id)
        return Result.success(movies)
    }

    override suspend fun prefetchTvShow(key: TvShowKey.Hosted): Result<Unit> {
        return getTvShow(key).map { }
    }

    override suspend fun prefetchTvShowByTmdbId(key: TvShowKey.Tmdb): Result<Unit> {
        logger.debug("Prefetching $key")
        val shows = hostedTvDataRepo.getTvShowsByTmdbId(key.id)
        logger.debug("Prefetching ${shows.size} show(s) for $key")
        val results = shows.parallelMap {
            val hostedShowKey = TvShowKey.Hosted(it.service, it.info.key)
            prefetchTvShow(hostedShowKey)
        }
        return if (results.any { it.isSuccess }) {
            logger.debug("Prefetching $key finished")
            Result.success(Unit)
        } else {
            logger.debug("Prefetching $key failed")
            Result.failure(Exception("All services failed!"))
        }
    }

    private suspend fun insertTvShowToDb(key: TvShowKey.Hosted, result: TvShowInfo) {
        val tmdbId = tmdbService.findTmdbId(SearchResult.Type.TV_SHOW, result.info)
                as? TmdbItemId.Tv
        logger.debug("Inserting ${result.info} with ${result.seasons.size} season(s)")
        val seasons = result.seasons
            .map { HostedSeason(key.streamingService, key.tvShowId, it.number) }
        val numToEp = result.seasons
            .flatMap { it.episodes.map { ep -> it.number to ep } }
            .map { HostedEpisode(key.streamingService, key.tvShowId, it.first, it.second) }
        hostedTvDataRepo.insertTvShow(
            HostedItem.TvShow(
                key.streamingService,
                result.info,
                tmdbId
            )
        )
        hostedTvDataRepo.insertSeasons(seasons)
        hostedTvDataRepo.insertEpisodes(numToEp)
        logger.debug("Inserted ${result.info} with ${result.seasons.size} season(s)")
    }

    override suspend fun insertHostedItem(item: HostedItem) {
        when (item) {
            is HostedItem.TvShow -> hostedTvDataRepo.insertTvShow(item)
            is HostedItem.Movie -> hostedTvDataRepo.insertMovie(item)
        }
    }

    override suspend fun insertHostedItems(items: List<HostedItem>) {
        logger.debug("Inserting ${items.size} hosted items")
        items.parallelMap {
            insertHostedItem(it)
        }
    }
}