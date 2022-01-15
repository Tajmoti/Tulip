package com.tajmoti.libtvprovider.southpark

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.TvProvider
import com.tajmoti.libtvprovider.model.*
import com.tajmoti.libtvprovider.southpark.model.Item
import com.tajmoti.libtvprovider.southpark.model.SouthParkEpisodeResponse
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

class SouthParkTvProvider(
    private val httpLoader: SimplePageSourceLoader,
    private val dispatcher: CoroutineContext = LibraryDispatchers.libraryContext,
) : TvProvider {
    private val deserializer = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://www.southparkstudios.com"
    private val info = TvItemInfo("South Park", "en", 1997)
    private val item = SearchResult.TvShow("8618fe26-7d1a-4b02-9119-752a92e72e4f", info)

    override suspend fun search(query: String): Result<List<SearchResult>> {
        val result = if (shouldReturnSouthPark(query)) {
            listOf(item)
        } else {
            emptyList<SearchResult>()
        }
        return Result.success(result)
    }

    private fun shouldReturnSouthPark(query: String): Boolean {
        val queryLowerCase = query.lowercase()
        val nameLowerCase = info.name.lowercase()
        return queryLowerCase.contains(nameLowerCase) || nameLowerCase.contains(query)
    }

    override suspend fun getTvShow(id: String): Result<TvItem.TvShow> {
        if (id != item.key)
            return Result.failure(IllegalArgumentException("South Park provider can only provide South Park"))
        return withContext(dispatcher) { getShowInfo() }
    }

    private suspend fun getShowInfo(): Result<TvItem.TvShow> {
        return getAllEpisodesUsingPages()
            .map(::groupAndSortEpisodes)
            .map(::seasonsToTvShow)
            .onFailure { logger.warn("Failed to fetch South Park episode data $it") }
    }

    private fun seasonsToTvShow(seasons: List<Season>): TvItem.TvShow {
        return TvItem.TvShow(info, seasons)
    }

    private fun groupAndSortEpisodes(allEpisodes: Set<TmpEpisodeData>): List<Season> {
        return allEpisodes.groupBy({ (s, _) -> s }, { (_, e) -> e })
            .map { (season, episodes) -> Season(season, episodes.sortedBy { episode -> episode.number }) }
            .sortedBy { season -> season.number }
    }

    private suspend fun getAllEpisodesUsingPages(): Result<Set<TmpEpisodeData>> {
        var page = 1
        val dst = mutableSetOf<TmpEpisodeData>()
        while (true) {
            val pageData = fetchSinglePaginatedEpisodeData(page)
                .getOrElse { return Result.failure(it) }
                .map { item -> parseEpisode(item) }
            dst += pageData
            if (pageData.any { isFirstEpisode(it) })
                return Result.success(dst)
            if (page > PAGE_LIMIT)
                return Result.failure(IllegalStateException("First episode not present in any of first $PAGE_LIMIT pages"))
            page++
        }
    }

    private fun isFirstEpisode(item: TmpEpisodeData): Boolean {
        return item.episode.number == 1 && item.season == 1
    }

    private fun parseEpisode(item: Item): TmpEpisodeData {
        val (s, e) = item.meta.header.title
            .split(" • ")
            .map { str -> str.filter { char -> char.isDigit() } }
            .map { it.toInt() }
        val episodeInfo = EpisodeInfo(item.url, e, item.meta.subHeader, item.meta.description, item.media.image.url)
        return TmpEpisodeData(s, episodeInfo)
    }

    private suspend fun fetchSinglePaginatedEpisodeData(page: Int): Result<List<Item>> {
        val url = "$baseUrl/api/episodes/$page/$MAX_PAGE_SIZE"
        return httpLoader(url).mapCatching(::deserializeSinglePaginatedEpisodeData)
    }

    private fun deserializeSinglePaginatedEpisodeData(json: String): List<Item> {
        val scrapedEpisode: SouthParkEpisodeResponse = deserializer.decodeFromString(json)
        return scrapedEpisode.items
    }

    override suspend fun getMovie(id: String): Result<TvItem.Movie> {
        return Result.failure(UnsupportedOperationException("Seriously?"))
    }

    override suspend fun getStreamableLinks(episodeOrMovieId: String): Result<List<VideoStreamRef>> {
        val url = baseUrl + episodeOrMovieId
        return Result.success(listOf(VideoStreamRef.Resolved("South Park", url)))
    }

    private data class TmpEpisodeData(
        val season: Int,
        val episode: EpisodeInfo
    )

    companion object {
        /**
         * Maximum page size supported by the API.
         * Anything larger clamps to this.
         */
        private const val MAX_PAGE_SIZE = 40

        /**
         * How many pages should be attempted at most before errorring out.
         */
        private const val PAGE_LIMIT = 40
    }
}