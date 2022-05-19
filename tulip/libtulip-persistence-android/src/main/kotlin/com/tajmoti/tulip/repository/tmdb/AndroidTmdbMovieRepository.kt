package com.tajmoti.tulip.repository.tmdb

import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.libtulip.repository.TmdbMovieRepository
import com.tajmoti.tulip.adapter.tmdb.TmdbMovieDbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbMovieDao
import com.tajmoti.tulip.mapper.tmdb.TmdbMovieMapper
import com.tajmoti.tulip.repository.RwRepositoryImpl
import javax.inject.Inject

class AndroidTmdbMovieRepository @Inject constructor(
    private val dao: TmdbMovieDao
) : TmdbMovieRepository, RwRepository<TulipMovie.Tmdb, MovieKey.Tmdb> by RwRepositoryImpl(
    dao = dao,
    adapter = TmdbMovieDbAdapter(),
    mapper = TmdbMovieMapper()
)
