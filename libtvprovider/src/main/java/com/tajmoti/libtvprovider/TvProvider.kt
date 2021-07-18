package com.tajmoti.libtvprovider

import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.libtvprovider.stream.Streamable
import java.io.Serializable

interface TvProvider {

    /**
     * Searches for shows or movies by their name.
     */
    suspend fun search(query: String): Result<List<TvItem>>

    /**
     * Retrieves a show by its key.
     */
    suspend fun getShow(key: Serializable): Result<TvItem.Show>

    /**
     * Retrieves a TV show season by its key.
     */
    suspend fun getSeason(key: Serializable): Result<Season>

    /**
     * Retrieves a streamable item by its key.
     */
    suspend fun getStreamable(key: Serializable): Result<Streamable>
}