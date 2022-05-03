package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbMovie
import kotlinx.coroutines.flow.Flow

class TmdbMovieDbAdapter : DbAdapter<TmdbDao, MovieKey.Tmdb, DbTmdbMovie> {

    override fun findByKeyFromDb(dao: TmdbDao, key: MovieKey.Tmdb): Flow<DbTmdbMovie?> {
        return dao.getMovie(key.id)
    }

    override suspend fun insertToDb(dao: TmdbDao, entity: DbTmdbMovie) {
        dao.insertMovie(entity)
    }

    override suspend fun insertToDb(dao: TmdbDao, entities: List<DbTmdbMovie>) {
        dao.insertMovies(entities)
    }
}