package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.*
import com.tajmoti.libtulip.misc.job.firstValueOrNull
import com.tajmoti.libtulip.model.NoSuccessfulResultsException
import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.HostedTvDataRepository
import com.tajmoti.libtulip.repository.ItemMappingRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.service.MappingSearchService
import com.tajmoti.libtvprovider.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MappingSearchServiceImpl(
    private val hostedRepository: HostedTvDataRepository,
    private val tmdbRepository: TmdbTvDataRepository,
    private val mappingRepository: ItemMappingRepository,
) : MappingSearchService {

    override fun searchAndCreateMappings(query: String): Flow<Result<List<MappedSearchResult>>> {
        return hostedRepository.search(query)
            .map { resMap -> resMap.takeUnless { it.all { (_, v) -> v.isFailure } } }
            .mapNotNullsWithContext(LibraryDispatchers.libraryContext, this::mapWithTmdbIds)
            .onEachNotNull(this::persistTmdbMappings)
            .onEachNull { logger.warn { "No successful results!" } }
            .mapFold({ Result.success(it) }, { Result.failure(NoSuccessfulResultsException) })
    }


    private suspend inline fun mapWithTmdbIds(resultMap: Map<StreamingService, Result<List<SearchResult>>>): List<MappedSearchResult> {
        val serviceToResults = resultMap
            .mapNotNull { (service, result) -> result.map{ service to it }.getOrNull() }
        val successfulItemsTv = serviceToResults
            .flatMap { (service, results) -> findTmdbIdForResults(service, results.mapNotNull { it as? SearchResult.TvShow }) }
        val successfulItemsMovie = serviceToResults
            .flatMap { (service, results) -> findTmdbIdForResultsMovie(service, results.mapNotNull { it as? SearchResult.Movie }) }
        return successfulItemsTv + successfulItemsMovie
    }

    private suspend fun findTmdbIdForResults(service: StreamingService, results: List<SearchResult.TvShow>): List<MappedSearchResult.TvShow> {
        return results.parallelMap { item -> findTvShowTmdbKey(item, service) }
    }

    private suspend fun findTmdbIdForResultsMovie(service: StreamingService, results: List<SearchResult.Movie>): List<MappedSearchResult.Movie> {
        return results.parallelMap { item -> findMovieTmdbKey(item, service) }
    }

    private suspend fun persistTmdbMappings(mapped: List<MappedSearchResult>) {
        mapped.parallelMap { searchResult -> persistTmdbMapping(searchResult) }
    }

    private suspend fun persistTmdbMapping(mapped: MappedSearchResult) {
        when (mapped) {
            is MappedSearchResult.Movie -> mapped.tmdbId
                ?.let { mappingRepository.createTmdbMappingMovie(it, mapped.key) }
            is MappedSearchResult.TvShow -> mapped.tmdbId
                ?.let { mappingRepository.createTmdbMappingTv(it, mapped.key) }
        }
    }

    private suspend fun findTvShowTmdbKey(item: SearchResult.TvShow, service: StreamingService): MappedSearchResult.TvShow {
        val tmdbId = tmdbRepository.findTvShowKey(item.info.name, item.info.firstAirDateYear).firstValueOrNull()
        val key = TvShowKey.Hosted(service, item.key)
        return MappedSearchResult.TvShow(key, item.info, tmdbId)
    }

    private suspend fun findMovieTmdbKey(item: SearchResult, service: StreamingService): MappedSearchResult.Movie {
        val tmdbId = tmdbRepository.findMovieKey(item.info.name, item.info.firstAirDateYear).firstValueOrNull()
        val key = MovieKey.Hosted(service, item.key)
        return MappedSearchResult.Movie(key, item.info, tmdbId)
    }
}