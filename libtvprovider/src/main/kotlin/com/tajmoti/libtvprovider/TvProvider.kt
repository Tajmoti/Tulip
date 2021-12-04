package com.tajmoti.libtvprovider

interface TvProvider {

    /**
     * Searches for shows or movies by their name.
     */
    suspend fun search(query: String): Result<List<SearchResult>>

    /**
     * Retrieves TV show info by its [id].
     */
    suspend fun getTvShow(id: String): Result<TvShowInfo>

    /**
     * Retrieves a movie by its information.
     */
    suspend fun getMovie(id: String): Result<MovieInfo>

    /**
     * Returns a list of links for the provided [MovieInfo] or [EpisodeInfo].
     */
    suspend fun getStreamableLinks(episodeOrMovieId: String): Result<List<VideoStreamRef>>
}