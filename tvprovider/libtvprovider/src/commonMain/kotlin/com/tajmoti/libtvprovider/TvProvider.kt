package com.tajmoti.libtvprovider

import com.tajmoti.libtvprovider.model.EpisodeInfo
import com.tajmoti.libtvprovider.model.SearchResult
import com.tajmoti.libtvprovider.model.TvItem
import com.tajmoti.libtvprovider.model.VideoStreamRef

interface TvProvider {
    /**
     * Searches for shows or movies by their name.
     * Return null if this provider should be ignored for the [query].
     */
    suspend fun search(query: String): Result<List<SearchResult>>?

    /**
     * Retrieves TV show info by its [id].
     */
    suspend fun getTvShow(id: String): Result<TvItem.TvShow>

    /**
     * Retrieves a movie by its information.
     */
    suspend fun getMovie(id: String): Result<TvItem.Movie>

    /**
     * Returns a list of links for the provided [TvItem.Movie] or [EpisodeInfo].
     */
    suspend fun getStreamableLinks(episodeOrMovieId: String): Result<List<VideoStreamRef>>
}