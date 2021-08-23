package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.mapToAsyncJobs
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.SearchService
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.libtvprovider.SearchResult
import javax.inject.Inject

class SearchServiceImpl @Inject constructor(
    private val tvDataService: TvDataService,
    private val hostedService: HostedTvDataService
) : SearchService {

    override suspend fun search(query: String): Result<List<TulipSearchResult>> {
        val hostedResults = hostedService.search(query)
            .getOrElse { return Result.failure(it) }
        val successfulItems = successfulResultsToHostedItems(hostedResults)
        logger.debug("Found {} results", successfulItems.size)
        hostedService.insertHostedItems(successfulItems)
        val zipped = hostedItemsToSearchResults(successfulItems)
        return Result.success(zipped)
    }


    private suspend fun successfulResultsToHostedItems(
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

    private suspend fun itemsToHostedItems(
        service: StreamingService,
        items: List<SearchResult>
    ): List<HostedItem> {
        val tmdbIds = mapToAsyncJobs(items) { tvDataService.findTmdbId(it.type, it.info) }
        return items.zip(tmdbIds) { item, tmdbId ->
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
}