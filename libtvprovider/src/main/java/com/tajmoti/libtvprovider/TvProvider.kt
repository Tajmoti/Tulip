package com.tajmoti.libtvprovider

import com.tajmoti.libtvprovider.show.Season
import com.tajmoti.libtvprovider.stream.Streamable

interface TvProvider {

    /**
     * Searches for shows or movies by their name.
     */
    suspend fun search(query: String): Result<List<TvItem>>

    /**
     * Retrieves a show by its key.
     */
    suspend fun getShow(key: String, info: TvItem.Show.Info): Result<TvItem.Show>

    /**
     * Retrieves a TV show season by its key.
     */
    suspend fun getSeason(key: String, info: Season.Info): Result<Season>

    /**
     * Retrieves a streamable item by its key.
     */
    suspend fun getStreamable(key: String, info: Streamable.Info): Result<Streamable>
}