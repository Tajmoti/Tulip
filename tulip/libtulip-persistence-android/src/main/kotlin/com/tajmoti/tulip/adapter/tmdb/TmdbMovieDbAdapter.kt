package com.tajmoti.tulip.adapter.tmdb

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbMovieDao
import com.tajmoti.tulip.entity.tmdb.TmdbMovie
import kotlinx.coroutines.flow.Flow

class TmdbMovieDbAdapter : DbAdapter<TmdbMovieDao, MovieKey.Tmdb, TmdbMovie> {

    override fun findByKeyFromDb(dao: TmdbMovieDao, key: MovieKey.Tmdb): Flow<TmdbMovie?> {
        return dao.getMovie(key.id)
    }

    override suspend fun insertToDb(dao: TmdbMovieDao, entity: TmdbMovie) {
        dao.insertMovie(entity)
    }

    override suspend fun insertToDb(dao: TmdbMovieDao, entities: List<TmdbMovie>) {
        dao.insertMovies(entities)
    }
}