package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.MovieKey
import kotlinx.coroutines.flow.Flow

interface MovieMappingRepository {

    fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>>

    suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb)
}