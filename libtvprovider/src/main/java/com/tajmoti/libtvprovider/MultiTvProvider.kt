package com.tajmoti.libtvprovider

import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.parallelMapToFlow
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout

class MultiTvProvider<ID>(
    private val providers: Map<ID, TvProvider>,
    /**
     * Timeout used for each of the providers.
     */
    private val timeoutMs: Long
) {

    /**
     * Searches [query] using all TV providers in parallel.
     */
    fun search(query: String): Flow<Pair<ID, Result<List<SearchResult>>>> {
        val results = providers.parallelMapToFlow { key, value ->
            key to searchAsync(query, key, value)
        }
        return results
    }

    private suspend inline fun searchAsync(
        query: String,
        id: ID,
        provider: TvProvider
    ): Result<List<SearchResult>> {
        logger.debug("Searching '$query' using $id")
        val result = searchWithTimeout(provider, query)
        logger.debug("Search for '$query' using $id ended with $result")
        return result
    }

    private suspend fun searchWithTimeout(
        it: TvProvider,
        query: String
    ): Result<List<SearchResult>> {
        return try {
            withTimeout(timeoutMs) {
                it.search(query)
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(e)
        }
    }

    suspend fun getShow(service: ID, id: String): Result<TvShowInfo> {
        return providers[service]!!.getTvShow(id)
    }

    suspend fun getMovie(service: ID, id: String): Result<MovieInfo> {
        return providers[service]!!.getMovie(id)
    }

    suspend fun getStreamableLinks(
        service: ID,
        episodeOrMovieKey: String
    ): Result<List<VideoStreamRef>> {
        return providers[service]!!.getStreamableLinks(episodeOrMovieKey)
    }
}