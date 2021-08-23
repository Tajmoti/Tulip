package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedMovie
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.TmdbItemId
import com.tajmoti.libtulip.repository.HostedMovieRepository
import com.tajmoti.libtvprovider.TvItemInfo
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.entity.hosted.DbMovie
import javax.inject.Inject

class AndroidHostedMovieRepository @Inject constructor(
    private val movieDao: MovieDao
) : HostedMovieRepository {

    override suspend fun getMovieByKey(service: StreamingService, key: String): HostedItem.Movie? {
        val db = movieDao.getByKey(service, key) ?: return null
        return dbMovieToLibMovie(db)
    }

    private fun dbMovieToLibMovie(db: DbMovie): HostedItem.Movie {
        val info = TvItemInfo(db.key, db.name, db.language, db.firstAirDateYear)
        return HostedItem.Movie(db.service, info, db.tmdbId?.let { TmdbItemId.Movie(it) })
    }

    private fun dbMovieToLibMovieX(db: DbMovie): HostedMovie {
        return HostedMovie(db.service, db.key, db.name, db.language)
    }

    override suspend fun getMovieByTmdbIdentifiers(tmdbItemId: TmdbItemId.Movie): List<HostedMovie> {
        return movieDao.getByTmdbId(tmdbItemId.id)
            .map { dbMovieToLibMovieX(it) }
    }

    override suspend fun insertMovie(movie: HostedItem.Movie) {
        val info = movie.info
        val db = DbMovie(
            movie.service,
            info.key,
            info.name,
            info.language,
            info.firstAirDateYear,
            movie.tmdbId?.id
        )
        movieDao.insert(db)
    }
}