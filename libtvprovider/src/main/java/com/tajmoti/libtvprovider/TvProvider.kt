package com.tajmoti.libtvprovider

interface TvProvider {

    /**
     * Searches for shows or movies by their name.
     */
    suspend fun search(query: String): Result<List<SearchResult>>

    /**
     * Retrieves TV show info by its [key].
     */
    suspend fun getTvShow(key: String): Result<TvShowInfo>

    /**
     * Retrieves a movie by its information.
     */
    suspend fun getMovie(movieKey: String): Result<MovieInfo>

    /**
     * Returns a list of links for the provided [MovieInfo] or [EpisodeInfo].
     */
    suspend fun getStreamableLinks(episodeOrMovieKey: String): Result<List<VideoStreamRef>>
}