package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.misc.*
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HostedTvDataRepositoryImpl @Inject constructor(
    private val hostedTvDataRepo: HostedInfoDataSource,
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val tmdbRepo: TmdbTvDataRepository,
    config: TulipConfiguration
) : HostedTvDataRepository {
    private val tvCache = TimedCache<TvShowKey.Hosted, TulipTvShowInfo.Hosted>(
        timeout = config.hostedItemCacheParams.validityMs, size = config.hostedItemCacheParams.size
    )
    private val movieCache = TimedCache<MovieKey.Hosted, TulipMovie.Hosted>(
        timeout = config.hostedItemCacheParams.validityMs, size = config.hostedItemCacheParams.size
    )
    private val streamCache = TimedCache<StreamableKey.Hosted, List<VideoStreamRef>>(
        timeout = config.streamCacheParams.validityMs, size = config.streamCacheParams.size
    )


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
        createTmdbMappings(mapped)
        return Result.success(mapped)
    }

    private suspend fun createTmdbMappings(mapped: List<TulipSearchResult>) {
        mapped.parallelMap { searchResult ->
            val tmdbId = searchResult.tmdbId ?: return@parallelMap
            searchResult.results.parallelMap { mappedResult ->
                hostedTvDataRepo.createTmdbMapping(mappedResult.key, tmdbId)
            }
        }
    }

    private suspend inline fun handleSearchResult(
        searchResults: Map<StreamingService, Result<List<SearchResult>>>
    ): List<TulipSearchResult> {
        val successfulItems = successfulResultsToHostedItems(searchResults)
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
        return items.parallelMapBoth { tmdbRepo.findTmdbId(it) }
            .map { (item, tmdbId) -> pairInfoWithTmdbId(item, service, tmdbId) }
    }

    private fun pairInfoWithTmdbId(
        item: SearchResult,
        service: StreamingService,
        tmdbId: TmdbItemId?
    ): MappedSearchResult {
        return when (item.type) {
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

    override suspend fun getTvShowAsFlow(key: TvShowKey.Hosted): NetFlow<out TulipTvShowInfo.Hosted> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResourceVariable(
            { hostedTvDataRepo.getTvShowByKey(key) },
            { fetchTvShow(key) },
            { hostedTvDataRepo.insertTvShow(it) },
            { it },
            { tvCache[key] },
            { tvCache[key] = it }
        )
    }

    override suspend fun getTvShow(key: TvShowKey.Hosted): Result<TulipTvShowInfo.Hosted> {
        return getTvShowAsFlow(key).firstOrNull()?.toResult()
            ?: Result.failure(MissingEntityException)
    }

    private suspend fun fetchTvShow(key: TvShowKey.Hosted): Result<TulipTvShowInfo.Hosted> {
        return tvProvider.getShow(key.streamingService, key.id)
            .map { it.fromNetwork(key, getTmdbIdForItem(it)) }
    }

    private suspend fun getTmdbIdForItem(it: TvShowInfo): TmdbItemId.Tv? {
        val result = SearchResult(it.info.id, SearchResult.Type.TV_SHOW, it.info)
        return tmdbRepo.findTmdbId(result) as? TmdbItemId.Tv
    }

    override suspend fun getSeason(key: SeasonKey.Hosted): Result<TulipSeasonInfo.Hosted> {
        logger.debug("Retrieving $key")
        return getTvShow(key.tvShowKey)
            .flatMap {
                it.seasons.firstOrNull { season -> season.key == key }
                    ?.let { season -> Result.success(season) }
                    ?: Result.failure(MissingEntityException)
            }
    }

    override suspend fun getSeasonsAsFlow(key: TvShowKey.Hosted): NetFlow<List<TulipSeasonInfo.Hosted>> {
        logger.debug("Retrieving $key")
        return getTvShowAsFlow(key)
            .map { it.map { tvShow -> tvShow.seasons } }
    }

    override suspend fun getSeasons(key: TvShowKey.Hosted): Result<List<TulipSeasonInfo.Hosted>> {
        return getSeasonsAsFlow(key).firstOrNull()?.toResult()
            ?: Result.failure(MissingEntityException)
    }

    override suspend fun getStreamableInfo(key: StreamableKey.Hosted): Result<StreamableInfoWithLanguage> {
        logger.debug("Retrieving $key")
        return when (key) {
            is EpisodeKey.Hosted -> getEpisodeInfo(key)
            is MovieKey.Hosted -> getMovieInfo(key)
        }
    }

    private suspend fun getEpisodeInfo(key: EpisodeKey.Hosted): Result<StreamableInfoWithLanguage> {
        return getTvShow(key.tvShowKey)
            .flatMap { tvShow ->
                toCompleteEpisodeInfo(tvShow, key)
                    ?.let { StreamableInfoWithLanguage(it, tvShow.language) }
                    ?.let { Result.success(it) }
                    ?: Result.failure(MissingEntityException)
            }
    }

    private fun toCompleteEpisodeInfo(
        tvShow: TulipTvShowInfo.Hosted,
        key: EpisodeKey.Hosted
    ): TulipCompleteEpisodeInfo.Hosted? {
        return tvShow.seasons.firstOrNull { season -> season.seasonNumber == key.seasonNumber }
            ?.episodes?.first { episode -> episode.key.id == key.id }
            ?.let { TulipCompleteEpisodeInfo.Hosted(key, tvShow.name, it) }
    }

    private suspend fun getMovieInfoAsFlow(key: MovieKey.Hosted): NetFlow<out TulipMovie.Hosted> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResourceVariable(
            { hostedTvDataRepo.getMovieByKey(key) },
            { fetchMovie(key) },
            { hostedTvDataRepo.insertMovie(it) },
            { it },
            { movieCache[key] },
            { movieCache[key] = it }
        )
    }

    private suspend fun fetchMovie(key: MovieKey.Hosted): Result<TulipMovie.Hosted> {
        return tvProvider.getMovie(key.streamingService, key.id)
            .map { it.fromNetwork(key, getTmdbIdForItem(it)) }
    }

    private suspend fun getTmdbIdForItem(it: MovieInfo): TmdbItemId.Movie? {
        val result = SearchResult(it.info.id, SearchResult.Type.TV_SHOW, it.info)
        return tmdbRepo.findTmdbId(result) as? TmdbItemId.Movie
    }

    private suspend fun getMovieInfo(key: MovieKey.Hosted): Result<StreamableInfoWithLanguage> {
        return getMovie(key)
            .map { StreamableInfoWithLanguage(it, it.language) }
    }

    private suspend fun getMovie(key: MovieKey.Hosted): Result<TulipMovie.Hosted> {
        return getMovieInfoAsFlow(key)
            .firstOrNull()?.toResult() ?: Result.failure(MissingEntityException)
    }

    private fun logExceptions(searchResult: Map<StreamingService, Result<List<SearchResult>>>) {
        for ((service, result) in searchResult) {
            val exception = result.exceptionOrNull() ?: continue
            logger.warn("{} failed with", service, exception)
        }
    }

    override suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Result<List<TulipEpisodeInfo.Hosted>> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepo.getTmdbMappingForTvShow(key.tvShowKey.id)
            .parallelMap { getHostedEpisodesByTmdbKey(it, key) }
            .mapNotNull { it.getOrNull() }
            .let { Result.success(it) }
    }

    private suspend fun getHostedEpisodesByTmdbKey(
        it: TvShowKey.Hosted,
        key: EpisodeKey.Tmdb
    ): Result<TulipEpisodeInfo.Hosted> {
        return getTvShow(it).flatMap { tvShow ->
            getEpisodeFromTv(tvShow, key)
                ?.let { episode -> Result.success(episode) }
                ?: Result.failure(MissingEntityException)
        }
    }

    private fun getEpisodeFromTv(
        tvShow: TulipTvShowInfo.Hosted,
        key: EpisodeKey.Tmdb
    ): TulipEpisodeInfo.Hosted? {
        return tvShow.seasons.firstOrNull { it.seasonNumber == key.seasonKey.seasonNumber }
            ?.episodes?.firstOrNull { it.episodeNumber == key.episodeNumber }
    }

    override suspend fun getCompleteEpisodesByTmdbId(key: EpisodeKey.Tmdb): Result<List<TulipCompleteEpisodeInfo.Hosted>> {
        return getEpisodeByTmdbId(key)
            .map {
                it.mapNotNull { episode ->
                    getTvShow(episode.key.tvShowKey)
                        .map { tv ->
                            TulipCompleteEpisodeInfo.Hosted(episode.key, tv.name, episode)
                        }
                        .onFailure { logger.warn("Failed to fetch TV for $episode") }
                        .getOrNull()
                }
            }
    }

    override suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<TulipMovie.Hosted>> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepo.getTmdbMappingForMovie(key.id)
            .parallelMapBoth { getMovie(it) }
            .onEach { (key, it) ->
                it.exceptionOrNull()?.let { logger.warn("Failed to fetch $key") }
            }
            .mapNotNull { it.second.getOrNull() }
            .let { Result.success(it) }
    }

    private suspend inline fun fetchStreamsAsFlow(key: StreamableKey.Hosted): NetFlow<List<VideoStreamRef>> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResource(
            { null },
            { tvProvider.getStreamableLinks(key.streamingService, key.id) },
            { },
            { streamCache[key] },
            { streamCache[key] = it }
        )
    }

    override suspend fun fetchStreams(key: StreamableKey.Hosted): Result<List<VideoStreamRef>> {
        val result = fetchStreamsAsFlow(key).firstOrNull()?.toResult()
            ?: Result.failure(MissingEntityException)
        return result.onFailure { logger.warn("Failed to fetch streams for $key", it) }
    }
}