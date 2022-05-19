package com.tajmoti.libtulip.facade

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.dto.SearchResultDto
import com.tajmoti.libtulip.dto.SearchResultGroupDto
import com.tajmoti.libtulip.dto.TvItemInfoDto
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.result.firstValueOrNull
import com.tajmoti.libtulip.service.ItemMappingRepository
import com.tajmoti.libtulip.service.TmdbTvDataRepository
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class SearchFacadeImpl(
    private val tvProvider: MultiTvProvider<StreamingService>,
    private val tmdbRepository: TmdbTvDataRepository,
    private val mappingRepository: ItemMappingRepository,
) : SearchFacade {

    private fun searchInternal(query: String): Flow<Map<StreamingService, Result<List<SearchResult>>>> {
        logger.debug { "Searching '$query'" }
        return tvProvider.search(query)
            .onEach { it.onEach { (service, listResult) -> logExceptions(service, listResult) } }
    }

    private fun logExceptions(service: StreamingService, result: Result<List<SearchResult>>) {
        val exception = result.exceptionOrNull() ?: return
        logger.warn(exception) { "$service failed" }
    }

    override fun search(query: String): Flow<Result<List<SearchResultDto>>> {
        return searchInternal(query)
            .map { resMap -> resMap.takeUnless { it.all { (_, v) -> v.isFailure } } }
            .mapNotNulls(this::mapWithTmdbIds)
            .onEachNotNull(this::persistTmdbMappings)
            .onEachNull { logger.warn { "No successful results!" } }
            .mapNotNulls { groupAndSortMappedResults(it) }
            .mapFold({ Result.success(it) }, { Result.failure(NoSuccessfulResultsException) })
    }


    private fun groupAndSortMappedResults(it: List<SearchResultGroupDto>): List<SearchResultDto> {
        return groupSearchResults(it).sortedWith(groupComparator)
    }

    private val groupComparator = Comparator<SearchResultDto> { a, b ->
        val typeA = getItemType(a)
        val typeB = getItemType(b)
        typeA.compareTo(typeB)
    }

    private fun getItemType(a: SearchResultDto?) = when (a) {
        is SearchResultDto.TvShow, is SearchResultDto.Movie -> 0
        else -> 1
    }

    private fun groupSearchResults(it: List<SearchResultGroupDto>): List<SearchResultDto> {
        val tvShows = it.mapNotNull { it as? SearchResultGroupDto.TvShow }
        val movies = it.mapNotNull { it as? SearchResultGroupDto.Movie }
        return groupItemsByTmdbIds(tvShows) + groupItemsByTmdbIdsMovie(movies)
    }

    private fun groupItemsByTmdbIds(items: List<SearchResultGroupDto.TvShow>): List<SearchResultDto> {
        return items
            .groupBy { it.tmdbId }
            .mapNotNull { (key, value) ->
                key?.let { SearchResultDto.TvShow(key, value) } ?: SearchResultDto.UnrecognizedTvShow(value)
            }
    }

    private fun groupItemsByTmdbIdsMovie(items: List<SearchResultGroupDto.Movie>): List<SearchResultDto> {
        return items
            .groupBy { it.tmdbId }
            .mapNotNull { (key, value) ->
                key?.let { SearchResultDto.Movie(key, value) } ?: SearchResultDto.UnrecognizedMovie(value)
            }
    }

    private suspend inline fun mapWithTmdbIds(resultMap: Map<StreamingService, Result<List<SearchResult>>>): List<SearchResultGroupDto> {
        val serviceToResults = resultMap
            .mapNotNull { (service, result) -> result.map { service to it }.getOrNull() }
        val successfulItemsTv = serviceToResults
            .flatMap { (service, results) ->
                findTmdbIdForResults(
                    service,
                    results.mapNotNull { it as? SearchResult.TvShow })
            }
        val successfulItemsMovie = serviceToResults
            .flatMap { (service, results) ->
                findTmdbIdForResultsMovie(
                    service,
                    results.mapNotNull { it as? SearchResult.Movie })
            }
        return successfulItemsTv + successfulItemsMovie
    }

    private suspend fun findTmdbIdForResults(
        service: StreamingService,
        results: List<SearchResult.TvShow>
    ): List<SearchResultGroupDto.TvShow> {
        return results.parallelMap { item -> findTvShowTmdbKey(item, service) }
    }

    private suspend fun findTmdbIdForResultsMovie(
        service: StreamingService,
        results: List<SearchResult.Movie>
    ): List<SearchResultGroupDto.Movie> {
        return results.parallelMap { item -> findMovieTmdbKey(item, service) }
    }

    private suspend fun persistTmdbMappings(mapped: List<SearchResultGroupDto>) {
        mapped.parallelMap { searchResult -> persistTmdbMapping(searchResult) }
    }

    private suspend fun persistTmdbMapping(mapped: SearchResultGroupDto) {
        when (mapped) {
            is SearchResultGroupDto.Movie -> mapped.tmdbId
                ?.let { mappingRepository.createTmdbMappingMovie(it, mapped.key) }
            is SearchResultGroupDto.TvShow -> mapped.tmdbId
                ?.let { mappingRepository.createTmdbMappingTv(it, mapped.key) }
        }
    }

    private suspend fun findTvShowTmdbKey(
        item: SearchResult.TvShow,
        service: StreamingService
    ): SearchResultGroupDto.TvShow {
        val tmdbId = tmdbRepository.findTvShowKey(item.info.name, item.info.firstAirDateYear).firstValueOrNull()
        val key = TvShowKey.Hosted(service, item.key)
        val info = item.info
        return SearchResultGroupDto.TvShow(key, TvItemInfoDto(info.name, info.language, info.firstAirDateYear), tmdbId)
    }

    private suspend fun findMovieTmdbKey(item: SearchResult, service: StreamingService): SearchResultGroupDto.Movie {
        val tmdbId = tmdbRepository.findMovieKey(item.info.name, item.info.firstAirDateYear).firstValueOrNull()
        val key = MovieKey.Hosted(service, item.key)
        val info = item.info
        return SearchResultGroupDto.Movie(key, TvItemInfoDto(info.name, info.language, info.firstAirDateYear), tmdbId)
    }
}