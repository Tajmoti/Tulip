package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.repository.HostedMovieRepository
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.adapter.HostedMovieDbAdapter
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.dao.hosted.TmdbMappingDao
import com.tajmoti.tulip.mapper.AndroidHostedMovieMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AndroidHostedMovieRepository @Inject constructor(
    private val dao: MovieDao,
    private val tmdbMappingDao: TmdbMappingDao
) : HostedMovieRepository {
    private val mapper = AndroidHostedMovieMapper()
    private val adapter = HostedMovieDbAdapter()

    override fun findByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        val tmdbMappingFlow = tmdbMappingDao.getTmdbIdByHostedKey(key.streamingService, key.id)
        val movieFlow = dao.getByKey(key.streamingService, key.id)
        return combine(tmdbMappingFlow, movieFlow) { tmdbId, item ->
            val tmdbKey = tmdbId?.let { MovieKey.Tmdb(it.tmdbId) }
            item?.let { mapper.fromDb(it, tmdbKey) }
        }
    }

    override suspend fun insert(repo: TulipMovie.Hosted) {
        adapter.insertToDb(dao, mapper.toDb(repo))
    }
}
