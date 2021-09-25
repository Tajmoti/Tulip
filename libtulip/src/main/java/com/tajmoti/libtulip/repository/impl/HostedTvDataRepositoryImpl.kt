package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.hosted.*
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.search.TulipSearchResult
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
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
        return hostedItemsToSearchResults(successfulItems)
    }

    private suspend inline fun successfulResultsToHostedItems(
        results: Map<StreamingService, Result<List<SearchResult>>>
    ): List<MappedSearchResult> {
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
    ): List<MappedSearchResult> {
        return items.parallelMapBoth { tmdbRepo.findTmdbId(it.type, it.info) }
            .map { (item, tmdbId) ->
                when (item.type) {
                    SearchResult.Type.TV_SHOW -> {
                        val key = TvShowKey.Hosted(service, item.key)
                        MappedSearchResult.TvShow(key, item.info, tmdbId as? TmdbItemId.Tv?)
                    }
                    SearchResult.Type.MOVIE -> {
                        val key = MovieKey.Hosted(service, item.key)
                        MappedSearchResult.Movie(key, item.info, tmdbId as? TmdbItemId.Movie?)
                    }
                }
            }
    }

    private fun hostedItemsToSearchResults(items: List<MappedSearchResult>): List<TulipSearchResult> {
        val idToItems = items.groupBy { it.tmdbId }
        val recognized = idToItems
            .filterKeys { it != null }
            .map { entry -> groupedItemToResult(entry.key!!, entry.value) }
        val unrecognized = idToItems[null]
            ?.let { listOf(TulipSearchResult.Unrecognized(it)) }
            ?: emptyList()
        return recognized + unrecognized
    }

    private fun groupedItemToResult(
        id: TmdbItemId,
        items: List<MappedSearchResult>
    ): TulipSearchResult {
        return when (id) {
            is TmdbItemId.Tv -> {
                val mapped = items.map { it as MappedSearchResult.TvShow }
                TulipSearchResult.TvShow(id, mapped)
            }
            is TmdbItemId.Movie -> {
                val mapped = items.map { it as MappedSearchResult.Movie }
                TulipSearchResult.Movie(id, mapped)
            }
        }
    }

    override suspend fun getTvShow(key: TvShowKey.Hosted): Result<TulipTvShowInfo.Hosted> {
        logger.debug("Retrieving {}", key)
        val result = tvProvider.getShow(key.streamingService, key.id)
            .map {
                val tmdbId = tmdbRepo.findTmdbId(SearchResult.Type.TV_SHOW, it.info)
                        as? TmdbItemId.Tv
                fromNetwork(it, key, tmdbId)
            }
            .onFailure { logger.warn("Failed to retrieve TV Show $key", it) }
            .onSuccess { hostedTvDataRepo.insertTvShow(it) }
            .getOrElse { getShowFromDb(key).getOrNull() }
            ?: return Result.failure(MissingEntityException) // TODO Better handling here
        return Result.success(result)
    }

    private fun fromNetwork(
        it: TvShowInfo,
        key: TvShowKey.Hosted,
        tmdbId: TmdbItemId.Tv?
    ): TulipTvShowInfo.Hosted {
        val seasons = it.seasons.map { fromNetwork(key, it) }
        return TulipTvShowInfo.Hosted(key, it.info, tmdbId, seasons)
    }

    private fun fromNetwork(tvShowKey: TvShowKey.Hosted, season: Season): TulipSeasonInfo.Hosted {
        val key = SeasonKey.Hosted(tvShowKey, season.number)
        val episodes = season.episodes.map { fromNetwork(key, it) }
        return TulipSeasonInfo.Hosted(key, episodes)
    }

    private fun fromNetwork(
        seasonKey: SeasonKey.Hosted,
        episode: EpisodeInfo
    ): TulipEpisodeInfo.Hosted {
        val key = EpisodeKey.Hosted(seasonKey, episode.key)
        return TulipEpisodeInfo.Hosted(key, episode.number, episode.name)
    }

    private suspend fun getShowFromDb(key: TvShowKey.Hosted): Result<TulipTvShowInfo.Hosted> {
        val show = hostedTvDataRepo.getTvShowByKey(key)
            ?: return Result.failure(MissingEntityException)
        return Result.success(show)
    }

    override suspend fun getSeason(key: SeasonKey.Hosted): Result<TulipSeasonInfo.Hosted> {
        logger.debug("Retrieving {}", key)
        return tvProvider.getShow(key.tvShowKey.streamingService, key.tvShowKey.id)
            .map {
                it.seasons.first { season -> season.number == key.seasonNumber }
                    .let { season -> fromNetwork(key.tvShowKey, season) }
            }
    }

    override suspend fun getSeasons(key: TvShowKey.Hosted): Result<List<TulipSeasonInfo.Hosted>> {
        logger.debug("Retrieving {}", key)
        return tvProvider.getShow(key.streamingService, key.id)
            .map { it.seasons.map { season -> fromNetwork(key, season) } }
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
        val info = TulipCompleteEpisodeInfo.Hosted(
            key = key,
            showName = sh.name,
            info = ep
        )
        return Result.success(StreamableInfoWithLanguage(info, shs.language))
    }

    private suspend fun getMovieInfo(key: MovieKey.Hosted): Result<StreamableInfoWithLanguage> {
        val movie = hostedTvDataRepo.getMovieByKey(key) ?: run {
            logger.warn("{} not found", key)
            return Result.failure(MissingEntityException)
        }
        return Result.success(StreamableInfoWithLanguage(movie, movie.language))
    }

    private fun logExceptions(searchResult: Map<StreamingService, Result<List<SearchResult>>>) {
        for ((service, result) in searchResult) {
            val exception = result.exceptionOrNull() ?: continue
            logger.warn("{} failed with", service, exception)
        }
    }

    override suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Result<List<TulipEpisodeInfo.Hosted>> {
        logger.debug("Retrieving episode by $key")
        prefetchTvShowByTmdbId(key.tvShowKey)
        val episodes = hostedTvDataRepo.getEpisodeByTmdbId(key)
        logger.debug("Episode retrieved by $key")
        return Result.success(episodes)
    }

    override suspend fun getCompleteEpisodesByTmdbId(key: EpisodeKey.Tmdb): Result<List<TulipCompleteEpisodeInfo.Hosted>> {
        return getEpisodeByTmdbId(key) // TODO Clean up this mess
            .map {
                it.mapNotNull { episode ->
                    getTvShow(episode.key.tvShowKey)
                        .map { tv ->
                            TulipCompleteEpisodeInfo.Hosted(
                                episode.key,
                                tv.name,
                                episode
                            )
                        }
                        .onFailure { logger.warn("Failed to fetch TV for $episode") }
                        .getOrNull()
                }
            }
    }

    override suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<TulipMovie.Hosted>> {
        val movies = hostedTvDataRepo.getMovieByTmdbKey(key)
        return Result.success(movies)
    }

    override suspend fun prefetchTvShow(key: TvShowKey.Hosted): Result<Unit> {
        return getTvShow(key).map { }
    }

    override suspend fun prefetchTvShowByTmdbId(key: TvShowKey.Tmdb): Result<Unit> {
        logger.debug("Prefetching $key")
        val shows = hostedTvDataRepo.getTvShowsByTmdbId(key) // TODO Remove and cache
        logger.debug("Prefetching ${shows.size} show(s) for $key")
        val results = shows.parallelMap {
            prefetchTvShow(it.key)
        }
        return if (results.any { it.isSuccess }) {
            logger.debug("Prefetching $key finished")
            Result.success(Unit)
        } else {
            logger.debug("Prefetching $key failed")
            Result.failure(Exception("All services failed!"))
        }
    }

    private suspend fun insertTvShowToDb(result: TulipTvShowInfo.Hosted) {
        hostedTvDataRepo.insertTvShow(result)
    }

    private suspend inline fun insertHostedItem(item: TulipItem.Hosted) {
        when (item) {
            is TulipTvShowInfo.Hosted -> hostedTvDataRepo.insertTvShow(item)
            is TulipMovie.Hosted -> hostedTvDataRepo.insertMovie(item)
        }
    }


    override suspend fun fetchStreams(key: StreamableKey.Hosted): Result<List<VideoStreamRef>> {
        val service = key.streamingService
        val streamableKey = key.id
        return tvProvider.getStreamableLinks(service, streamableKey)
            .onFailure { logger.warn("Failed to fetch streams for $service $streamableKey", it) }
    }
}