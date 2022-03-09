package com.tajmoti.libtvprovider

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.flatMap
import com.tajmoti.commonutils.combineRunningFold
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItem
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class MultiTvProvider<S>(
    private val providers: Map<S, TvProvider>,
    /**
     * Timeout used for each of the providers.
     */
    private val timeoutMs: Long
) {

    /**
     * Searches [query] using all TV providers in parallel.
     */
    fun search(query: String): Flow<Map<S, Result<List<SearchResult>>>> {
        return providers
            .map { (service, provider) -> searchAsFlow(provider, query).mapWithContext(LibraryDispatchers.libraryContext) { result -> (service to result) } }
            .combineRunningFold()
            .mapWithContext(LibraryDispatchers.libraryContext) { it.toMap() }
    }

    private fun searchAsFlow(provider: TvProvider, query: String): Flow<Result<List<SearchResult>>> {
        return flow { search(provider, query)?.let { emit(it) } }
    }

    private suspend fun search(provider: TvProvider, query: String): Result<List<SearchResult>>? {
        return try {
            withTimeout(timeoutMs) { provider.search(query) }
        } catch (e: TimeoutCancellationException) {
            Result.failure(e)
        }
    }

    suspend fun getShow(service: S, id: String): Result<TvItem.TvShow> {
        return getProvider(service).flatMap { it.getTvShow(id) }
    }

    suspend fun getMovie(service: S, id: String): Result<TvItem.Movie> {
        return getProvider(service).flatMap { it.getMovie(id) }
    }

    suspend fun getStreamableLinks(service: S, episodeOrMovieKey: String): Result<List<VideoStreamRef>> {
        return getProvider(service).flatMap { it.getStreamableLinks(episodeOrMovieKey) }
    }


    private fun getProvider(service: S): Result<TvProvider> {
        return providers[service]?.let { Result.success(it) }
            ?: return Result.failure(IllegalArgumentException("No provider for $service"))
    }
}