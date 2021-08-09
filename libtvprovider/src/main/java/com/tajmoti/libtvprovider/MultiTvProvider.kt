package com.tajmoti.libtvprovider

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MultiTvProvider<ID>(
    private val providers: Map<ID, TvProvider>
) {
    suspend fun search(query: String): List<Pair<ID, Result<List<TvItem>>>> {
        val coroutines = coroutineScope {
            providers.map { async { it.value.search(query) } }
        }
        val ids = providers.map { it.key }
        val results = awaitAll(*coroutines.toTypedArray())
        return ids.zip(results)
    }

    suspend fun getShow(service: ID, info: TvItem.Show.Info): Result<TvItem.Show> {
        return providers[service]!!.getShow(info)
    }

    suspend fun getSeason(service: ID, info: Season.Info): Result<Season> {
        return providers[service]!!.getSeason(info)
    }

    suspend fun getEpisode(service: ID, info: Episode.Info): Result<Episode> {
        return providers[service]!!.getEpisode(info)
    }

    suspend fun getMovie(service: ID, info: TvItem.Movie.Info): Result<TvItem.Movie> {
        return providers[service]!!.getMovie(info)
    }
}