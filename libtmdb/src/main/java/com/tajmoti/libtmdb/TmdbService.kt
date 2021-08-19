package com.tajmoti.libtmdb

import com.tajmoti.libtmdb.model.FindResponse
import com.tajmoti.libtmdb.model.SearchMovieResponse
import com.tajmoti.libtmdb.model.SearchTvResponse
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
}