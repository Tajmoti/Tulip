package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

/**
 * Stores mappings between [ItemKey.Tmdb] and [ItemKey.Hosted].
 */
interface ItemMappingRepository {
    /**
     * Creates a mapping between the [hostedKey] and the [tmdbKey].
     * This is a one to many relationship - one [tmdbKey] can have multiple [hostedKey]s.
     */
    suspend fun createTmdbMapping(tmdbKey: ItemKey.Tmdb, hostedKey: ItemKey.Hosted)

    /**
     * Retrieves all [TvShowKey.Hosted] keys that were paired with [key].
     */
    fun getHostedTvShowKeysByTmdbKey(key: TvShowKey.Tmdb): Flow<List<TvShowKey.Hosted>>

    /**
     * Retrieves all [MovieKey.Hosted] keys that were paired with [key].
     */
    fun getHostedMovieKeysByTmdbKey(key: MovieKey.Tmdb): Flow<List<MovieKey.Hosted>>
}