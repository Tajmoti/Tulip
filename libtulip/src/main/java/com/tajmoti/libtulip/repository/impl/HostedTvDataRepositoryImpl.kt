package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.misc.cache.TimedCache
import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.misc.job.firstValueOrNull
import com.tajmoti.libtulip.misc.job.getNetworkBoundResource
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.search.TulipSearchResult
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class HostedTvDataRepositoryImpl(
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


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun search(query: String): Flow<Result<List<TulipSearchResult>>> {
        logger.debug("Searching '{}'", query)
        return tvProvider.search(query)
            .onEach(this::logExceptions)
            .runningFoldConcatDropInitial()
            .map { resMap -> resMap.takeUnless { it.all { (_, v) -> v.isFailure } } }
            .mapNotNullsWithContext(Dispatchers.Default, this::handleSearchResult)
            .onEachNotNull(this::createTmdbMappings)
            .onEachNull { logger.warn("No successful results!") }
            .mapFold({ Result.success(it) }, { Result.failure(NoSuccessfulResultsException) })
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
        return items.parallelMapBoth { findTmdbIdOrNull(it) }
            .map { (item, tmdbId) -> pairInfoWithTmdbId(item, service, tmdbId) }
    }

    private fun pairInfoWithTmdbId(
        item: SearchResult,
        service: StreamingService,
        tmdbId: TvShowKey.Tmdb?
    ): MappedSearchResult {
        return when (item.type) {
            SearchResult.Type.TV_SHOW -> {
                val key = TvShowKey.Hosted(service, item.key)
                MappedSearchResult.TvShow(key, item.info, tmdbId)
            }
            SearchResult.Type.MOVIE -> {
                val key = MovieKey.Hosted(service, item.key)
                MappedSearchResult.Movie(key, item.info, tmdbId as? MovieKey.Tmdb?)
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
        id: ItemKey.Tmdb,
        items: List<MappedSearchResult>
    ): TulipSearchResult {
        return when (id) {
            is TvShowKey.Tmdb -> {
                val mapped = items.map { it as MappedSearchResult.TvShow }
                TulipSearchResult.TvShow(id, mapped)
            }
            is MovieKey.Tmdb -> {
                val mapped = items.map { it as MappedSearchResult.Movie }
                TulipSearchResult.Movie(id, mapped)
            }
        }
    }

    override fun getTvShow(key: TvShowKey.Hosted): NetFlow<TulipTvShowInfo.Hosted> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResource(
            { hostedTvDataRepo.getTvShowByKey(key) },
            { fetchTvShow(key) },
            { hostedTvDataRepo.insertTvShow(it) },
            { tvCache[key] },
            { tvCache[key] = it }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTvShowsByTmdbKey(key: TvShowKey.Tmdb): Flow<List<Result<TulipTvShowInfo.Hosted>>> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepo.getTmdbMappingForTvShow(key)
            .flatMapLatest { movieKeyList ->
                movieKeyList.map { getTvShow(it) }
                    .combine()
                    .mapNetworkResultToResultInListFlow()
            }
    }

    private suspend fun fetchTvShow(key: TvShowKey.Hosted): Result<TulipTvShowInfo.Hosted> {
        return tvProvider.getShow(key.streamingService, key.id)
            .map {
                // TODO Use flows properly
                val tmdbId = tmdbRepo.findTmdbIdTv(it.info.name, it.info.firstAirDateYear).firstValueOrNull()
                it.fromNetwork(key, tmdbId)
            }
    }

    override fun getSeason(key: SeasonKey.Hosted): Flow<NetworkResult<TulipSeasonInfo.Hosted>> {
        logger.debug("Retrieving $key")
        return getTvShow(key.tvShowKey)
            .map { it.convert { showInfo -> showInfo.findSeasonOrNull(key) } }
    }

    override fun getSeasons(key: TvShowKey.Hosted): NetFlow<List<TulipSeasonInfo.Hosted>> {
        logger.debug("Retrieving $key")
        return getTvShow(key)
            .map { it.map { tvShow -> tvShow.seasons } }
    }

    override fun getStreamableInfo(key: StreamableKey.Hosted): Flow<Result<StreamableInfo.Hosted>> {
        logger.debug("Retrieving $key")
        return when (key) {
            is EpisodeKey.Hosted -> getEpisodeInfo(key)
            is MovieKey.Hosted -> getMovie(key).map { it.toResult() }
        }
    }

    private fun getEpisodeInfo(key: EpisodeKey.Hosted): Flow<Result<StreamableInfo.Hosted>> {
        return getTvShow(key.tvShowKey)
            .map { netResult -> netResult.toResult().flatMap { tvShow -> tvShow.findCompleteEpisodeInfoAsResult(key) } }
    }

    override fun getMovie(key: MovieKey.Hosted): Flow<NetworkResult<TulipMovie.Hosted>> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResource(
            { hostedTvDataRepo.getMovieByKey(key) },
            { fetchMovie(key) },
            { hostedTvDataRepo.insertMovie(it) },
            { movieCache[key] },
            { movieCache[key] = it }
        )
    }

    private suspend fun fetchMovie(key: MovieKey.Hosted): Result<TulipMovie.Hosted> {
        return tvProvider.getMovie(key.streamingService, key.id)
            .map {
                // TODO Use flows properly
                val tmdbId = tmdbRepo.findTmdbIdMovie(it.info.name, it.info.firstAirDateYear).firstValueOrNull()
                it.fromNetwork(key, tmdbId)
            }
    }

    private fun logExceptions(searchResult: Pair<StreamingService, Result<List<SearchResult>>>) {
        val (service, result) = searchResult
        val exception = result.exceptionOrNull() ?: return
        logger.warn("{} failed with", service, exception)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Flow<List<Result<TulipEpisodeInfo.Hosted>>> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepo.getTmdbMappingForTvShow(key.tvShowKey)
            .flatMapLatest { movieKeyList -> movieKeyList.map { getHostedEpisodesByTmdbKey(it, key) }.combine() }
    }

    private fun getHostedEpisodesByTmdbKey(
        tvShowKey: TvShowKey.Hosted,
        episodeKey: EpisodeKey.Tmdb
    ): Flow<Result<TulipEpisodeInfo.Hosted>> {
        val tvShow = getTvShow(tvShowKey)
        return tvShow.map { it.toResult().flatMap { tvShow -> tvShow.findEpisodeAsResult(episodeKey) } }
    }


    override fun getCompleteEpisodesByTmdbKey(key: EpisodeKey.Tmdb): Flow<List<Result<TulipCompleteEpisodeInfo.Hosted>>> {
        return getTvShowsByTmdbKey(key.tvShowKey)
            .map { tvListResult ->
                tvListResult.map { tvList ->
                    tvList.flatMap { tv -> tv.findCompleteEpisodeFromTvAsResult(key) }
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMoviesByTmdbKey(key: MovieKey.Tmdb): Flow<List<Result<TulipMovie.Hosted>>> {
        logger.debug("Retrieving $key")
        return hostedTvDataRepo.getTmdbMappingForMovie(key)
            .flatMapLatest { movieKeyList ->
                movieKeyList.map { getMovie(it) }
                    .combine()
                    .mapNetworkResultToResultInListFlow()
            }
    }

    override fun fetchStreams(key: StreamableKey.Hosted): NetFlow<List<VideoStreamRef>> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResource(
            { null },
            { tvProvider.getStreamableLinks(key.streamingService, key.id) },
            { },
            { streamCache[key] },
            { streamCache[key] = it }
        )
    }

    private suspend fun findTmdbIdOrNull(searchResult: SearchResult): TvShowKey.Tmdb? {
        return when (searchResult.type) {
            SearchResult.Type.TV_SHOW ->
                tmdbRepo.findTmdbIdTv(searchResult.info.name, searchResult.info.firstAirDateYear).firstValueOrNull()
            SearchResult.Type.MOVIE ->
                tmdbRepo.findTmdbIdTv(searchResult.info.name, searchResult.info.firstAirDateYear).firstValueOrNull()
        }
    }

    private fun <T> T?.toResultIfMissing(): Result<T> {
        return this?.let { notNull -> Result.success(notNull) } ?: Result.failure(MissingEntityException)
    }

    private fun TulipTvShowInfo.Hosted.findEpisodeAsResult(
        key: EpisodeKey.Tmdb
    ) = findEpisodeOrNull(key).toResultIfMissing()

    private fun TulipTvShowInfo.Hosted.findCompleteEpisodeInfoAsResult(
        key: EpisodeKey.Hosted
    ) = findCompleteEpisodeInfo(key).toResultIfMissing()

    private fun TulipTvShowInfo.Hosted.findCompleteEpisodeFromTvAsResult(
        key: EpisodeKey.Tmdb
    ) = findEpisodeAsResult(key)
        .map { episode -> TulipCompleteEpisodeInfo.Hosted(episode.key, name, episode, language) }

    private fun <T> Flow<List<NetworkResult<T>>>.mapNetworkResultToResultInListFlow(): Flow<List<Result<T>>> {
        return map { it.map { networkResult -> networkResult.toResult() } }
    }
}