package com.tajmoti.libtvprovider

class MultiTvProvider<ID>(
    private val providers: Map<ID, TvProvider>
) {
    suspend fun search(query: String): List<Pair<ID, Result<List<TvItem>>>> {
        return providers.map { it.key to it.value.search(query) }
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