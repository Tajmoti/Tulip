package com.tajmoti.libtmdb

import com.tajmoti.libtmdb.model.find.FindResponse
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {

    @GET("3/find/{external_id}")
    suspend fun find(
        @Path("external_id") externalId: String,
        @Query("language") language: String = "en-US",
        @Query("external_source") externalSource: String = "imdb_id"
    ): FindResponse

    @GET("3/search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("first_air_date_year") firstAirDateYear: Int?
    ): SearchTvResponse

    @GET("3/search/movie")
    suspend fun searchMovie(
        @Query("query") query: String,
        @Query("first_air_date_year") firstAirDateYear: Int?
    ): SearchMovieResponse

    @GET("3/tv/{tv_id}")
    suspend fun getTv(
        @Path("tv_id") tvId: Long
    ): Tv

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun getSeason(
        @Path("tv_id") tvId: Long,
        @Path("season_number") seasonNumber: Int
    ): Season

    @GET("3/tv/{tv_id}/season/{season_number}/episode/{episode_number}")
    suspend fun getEpisode(
        @Path("tv_id") tvId: Long,
        @Path("season_number") seasonNumber: Int,
        @Path("episode_number") episodeNumber: Int,
    ): Episode

    @GET("3//movie/{movie_id}")
    suspend fun getMovie(
        @Path("movie_id") movieId: Long
    ): Movie
}