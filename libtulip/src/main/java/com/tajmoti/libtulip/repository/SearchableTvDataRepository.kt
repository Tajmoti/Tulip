package com.tajmoti.libtulip.repository

import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse

interface SearchableTvDataRepository : ReadOnlyTvDataRepository {

    suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse>

    suspend fun searchMovie(query: String, firstAirDateYear: Int?): Result<SearchMovieResponse>
}