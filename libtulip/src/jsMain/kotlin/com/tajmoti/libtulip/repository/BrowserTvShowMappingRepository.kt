package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserTvShowMappingRepository : TvShowMappingRepository {
    private val tmdbTvShowMappings = BrowserStorage<TvShowKey.Tmdb, Set<TvShowKey.Hosted>>()

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        tmdbTvShowMappings.update(tmdb) { oldValue -> (oldValue ?: mutableSetOf()).plus(hosted) }
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return tmdbTvShowMappings.get(tmdb).map { it ?: emptySet() }
    }
}