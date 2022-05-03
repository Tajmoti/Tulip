package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.libtulip.repository.TmdbMovieRepository
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.adapter.TmdbMovieDbAdapter
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.mapper.AndroidTmdbMovieMapper
import javax.inject.Inject

class AndroidTmdbMovieRepository @Inject constructor(
    private val dao: TmdbDao
) : TmdbMovieRepository, RwRepository<TulipMovie.Tmdb, MovieKey.Tmdb> by RwRepositoryImpl(
    dao = dao,
    adapter = TmdbMovieDbAdapter(),
    mapper = AndroidTmdbMovieMapper()
)
