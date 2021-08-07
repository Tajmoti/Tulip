package com.tajmoti.libtvprovider

interface TvProvider {

    /**
     * Searches for shows or movies by their name.
     */
    suspend fun search(query: String): Result<List<TvItem>>

    /**
     * Retrieves a show by its information.
     */
    suspend fun getShow(info: TvItem.Show.Info): Result<TvItem.Show>

    /**
     * Retrieves a TV show season by its information.
     */
    suspend fun getSeason(info: Season.Info): Result<Season>

    /**
     * Retrieves an episode by its information.
     */
    suspend fun getEpisode(info: Episode.Info): Result<Episode>

    /**
     * Retrieves a movie by its information.
     */
    suspend fun getMovie(info: TvItem.Movie.Info): Result<TvItem.Movie>
}