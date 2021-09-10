package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.hosted.*
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.info.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.service.impl.toInfo
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HostedTvDataRepositoryImpl @Inject constructor(
    private val hostedTvDataRepo: HostedInfoDataSource,
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val tmdbRepo: TmdbTvDataRepository
) : HostedTvDataRepository {

    override suspend fun search(query: String): Result<List<TulipSearchResult>> {
        logger.debug("Searching '{}'", query)
        val searchResult = tvProvider.search(query)
        if (searchResult.none { it.value.isSuccess }) {
            logger.warn("No successful results!")
            return Result.failure(NoSuccessfulResultsException)
        }
        logExceptions(searchResult)
        val mapped = withContext(Dispatchers.Default) {
            handleSearchResult(searchResult)
        }
        return Result.success(mapped)
    }

    private suspend inline fun handleSearchResult(
        searchResults: Map<StreamingService, Result<List<SearchResult>>>
    ): List<TulipSearchResult> {
        val successfulItems = successfulResultsToHostedItems(searchResults)
        logger.debug("Found {} results", successfulItems.size)
        insertHostedItems(successfulItems)
        return hostedItemsToSearchResults(successfulItems)
    }

    private suspend inline fun successfulResultsToHostedItems(
        results: Map<StreamingService, Result<List<SearchResult>>>
    ): List<HostedItem> {
        return results
            .mapNotNull { (service, itemListResult) ->
                val itemList = itemListResult
                    .getOrElse { return@mapNotNull null }
                service to itemList
            }
            .flatMap { (service, itemListResult) ->
                itemsToHostedItems(service, itemListResult)
            }
    }

    private suspend inline fun itemsToHostedItems(
        service: StreamingService,
        items: List<SearchResult>
    ): List<HostedItem> {
        return items.parallelMapBoth { tmdbRepo.findTmdbId(it.type, it.info) }
            .map { (item, tmdbId) ->
                when (item.type) {
                    SearchResult.Type.TV_SHOW ->
                        HostedItem.TvShow(service, item.info, tmdbId as? TmdbItemId.Tv?)
                    SearchResult.Type.MOVIE ->
                        HostedItem.Movie(service, item.info, tmdbId as? TmdbItemId.Movie?)
                }
            }
    }

    private fun hostedItemsToSearchResults(items: List<HostedItem>): List<TulipSearchResult> {
        val idToItems = items.groupBy { it.tmdbId }
        val recognized = idToItems
            .filterKeys { it != null }
            .map { entry -> groupedItemToResult(entry.key!!, entry.value) }
        val unrecognized = idToItems[null]
            ?.let { listOf(TulipSearchResult.Unrecognized(it)) }
            ?: emptyList()
        return recognized + unrecognized
    }

    private fun groupedItemToResult(id: TmdbItemId, items: List<HostedItem>): TulipSearchResult {
        return when (id) {
            is TmdbItemId.Tv -> {
                val mapped = items.map { it as HostedItem.TvShow }
                TulipSearchResult.TvShow(id, mapped)
            }
            is TmdbItemId.Movie -> {
                val mapped = items.map { it as HostedItem.Movie }
                TulipSearchResult.Movie(id, mapped)
            }
        }
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
            .map { (season, episodes) ->
                val seasonEpisodes = episodes.map { it.toInfo() }
                season.toInfo(seasonEpisodes)
            }
        val tvInfo = show.toInfo(key)
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
        val episodes = hostedTvDataRepo.getEpisodeByTmdbId(key)
        logger.debug("Episode retrieved by $key")
        return Result.success(episodes)
    }

    override suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<HostedMovie>> {
        val movies = hostedTvDataRepo.getMovieByTmdbKey(key)
        return Result.success(movies)
    }

    override suspend fun prefetchTvShow(key: TvShowKey.Hosted): Result<Unit> {
        return getTvShow(key).map { }
    }

    override suspend fun prefetchTvShowByTmdbId(key: TvShowKey.Tmdb): Result<Unit> {
        logger.debug("Prefetching $key")
        val shows = hostedTvDataRepo.getTvShowsByTmdbId(key)
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
        val tmdbId = tmdbRepo.findTmdbId(SearchResult.Type.TV_SHOW, result.info)
                as? TmdbItemId.Tv
        logger.debug("Inserting ${result.info} with ${result.seasons.size} season(s)")
        val seasons = result.seasons
            .map { HostedSeason(key.streamingService, key.tvShowId, it.number) }
        val episodes = result.seasons
            .flatMapWithTransform({ it.episodes }, { season, episode -> season.number to episode })
            .map { HostedEpisode(key.streamingService, key.tvShowId, it.first, it.second) }
        val tvShow = HostedItem.TvShow(key.streamingService, result.info, tmdbId)
        hostedTvDataRepo.insertTvShow(tvShow)
        hostedTvDataRepo.insertSeasons(seasons)
        hostedTvDataRepo.insertEpisodes(episodes)
        logger.debug("Inserted ${result.info} with ${result.seasons.size} season(s)")
    }

    private suspend inline fun insertHostedItem(item: HostedItem) {
        when (item) {
            is HostedItem.TvShow -> hostedTvDataRepo.insertTvShow(item)
            is HostedItem.Movie -> hostedTvDataRepo.insertMovie(item)
        }
    }

    private suspend inline fun insertHostedItems(items: List<HostedItem>) {
        logger.debug("Inserting ${items.size} hosted items")
        items.parallelMap {
            insertHostedItem(it)
        }
    }


    override suspend fun fetchStreams(key: StreamableKey.Hosted): Result<List<VideoStreamRef>> {
        val service = key.streamingService
        val streamableKey = key.streamableKey
        return tvProvider.getStreamableLinks(service, streamableKey)
            .onFailure { logger.warn("Failed to fetch streams for $service $streamableKey", it) }
    }
}