package com.tajmoti.tulip.repository.hosted

import com.tajmoti.libtulip.repository.HostedMovieRepository
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.adapter.hosted.HostedMovieDbAdapter
import com.tajmoti.tulip.dao.hosted.HostedMovieDao
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.mapper.hosted.HostedMovieMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AndroidHostedMovieRepository @Inject constructor(
    private val dao: HostedMovieDao,
    private val itemMappingDao: ItemMappingDao
) : HostedMovieRepository {
    private val mapper = HostedMovieMapper()
    private val adapter = HostedMovieDbAdapter()

    override fun findByKey(key: MovieKey.Hosted): Flow<TulipMovie.Hosted?> {
        val tmdbMappingFlow = itemMappingDao.getTmdbIdByHostedKey(key.streamingService, key.id)
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
