package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.repository.MovieRepository
import com.tajmoti.tulip.db.dao.MovieDao
import com.tajmoti.tulip.db.entity.DbMovie
import javax.inject.Inject

class AndroidMovieRepository @Inject constructor(
    private val movieDao: MovieDao
) : MovieRepository {
    override suspend fun getMovieByKey(service: StreamingService, key: String): TulipMovie? {
        val db = movieDao.getByKey(service, key) ?: return null
        return TulipMovie(db.service, db.key, db.name, db.language)
    }

    override suspend fun insertMovie(movie: TulipMovie) {
        val db = DbMovie(movie.service, movie.key, movie.name, movie.language)
        movieDao.insert(db)
    }
}