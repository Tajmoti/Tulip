package com.tajmoti.libtvprovider

import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.libtvprovider.stream.Streamable

class MultiTvProvider<ID>(
    private val providers: Map<ID, TvProvider>
) {
    suspend fun search(query: String): List<Pair<ID, Result<List<TvItem>>>> {
        return providers.map { it.key to it.value.search(query) }
    }

    suspend fun getShow(service: ID, key: String, info: TvItem.Show.Info): Result<TvItem.Show> {
        return providers[service]!!.getShow(key, info)
    }

    suspend fun getSeason(service: ID, key: String, info: Season.Info): Result<Season> {
        return providers[service]!!.getSeason(key, info)
    }

    suspend fun getStreamable(service: ID, key: String, info: Streamable.Info): Result<Streamable> {
        return providers[service]!!.getStreamable(key, info)
    }
}