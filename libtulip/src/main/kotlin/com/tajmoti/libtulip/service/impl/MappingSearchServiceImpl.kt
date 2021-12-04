package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.misc.job.firstValueOrNull
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.search.TulipSearchResult
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtvprovider.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MappingSearchServiceImpl(
    private val hostedRepository: HostedTvDataRepository,
    private val tmdbRepository: TmdbTvDataRepository,
    private val hostedToTmdbMappingRepository: ItemMappingRepository,
) : MappingSearchService {

    override fun searchAndCreateMappings(query: String): Flow<Result<List<TulipSearchResult>>> {
        return hostedRepository.search(query)
            .map { resMap -> resMap.takeUnless { it.all { (_, v) -> v.isFailure } } }
            .mapNotNullsWithContext(Dispatchers.Default, this::handleSearchResult)
            .onEachNotNull(this::createTmdbMappings)
            .onEachNull { logger.warn("No successful results!") }
            .mapFold({ Result.success(it) }, { Result.failure(NoSuccessfulResultsException) })
    }


    private suspend fun createTmdbMappings(mapped: List<TulipSearchResult>) {
        mapped.parallelMap { searchResult -> createTmdbMapping(searchResult) }
    }

    private suspend fun createTmdbMapping(searchResult: TulipSearchResult) {
        val tmdbId = searchResult.tmdbId ?: return
        searchResult.results.parallelMap { mappedResult ->
            hostedToTmdbMappingRepository.createTmdbMapping(tmdbId, mappedResult.key)
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
        tmdbId: ItemKey.Tmdb?
    ): MappedSearchResult {
        return when (item.type) {
            SearchResult.Type.TV_SHOW -> {
                val key = TvShowKey.Hosted(service, item.key)
                MappedSearchResult.TvShow(key, item.info, tmdbId as? TvShowKey.Tmdb)
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


    private suspend fun findTmdbIdOrNull(searchResult: SearchResult): ItemKey.Tmdb? {
        return when (searchResult.type) {
            SearchResult.Type.TV_SHOW ->
                tmdbRepository.findTvShowKey(searchResult.info.name, searchResult.info.firstAirDateYear)
                    .firstValueOrNull()
            SearchResult.Type.MOVIE ->
                tmdbRepository.findMovieKey(searchResult.info.name, searchResult.info.firstAirDateYear)
                    .firstValueOrNull()
        }
    }
}