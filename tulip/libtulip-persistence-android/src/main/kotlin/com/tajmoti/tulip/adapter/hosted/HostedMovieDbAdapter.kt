package com.tajmoti.tulip.adapter.hosted

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.hosted.HostedMovieDao
import com.tajmoti.tulip.entity.hosted.HostedMovie
import kotlinx.coroutines.flow.Flow

class HostedMovieDbAdapter : DbAdapter<HostedMovieDao, MovieKey.Hosted, HostedMovie> {

    override fun findByKeyFromDb(dao: HostedMovieDao, key: MovieKey.Hosted): Flow<HostedMovie?> {
        return dao.getByKey(key.streamingService, key.id)
    }

    override suspend fun insertToDb(dao: HostedMovieDao, entity: HostedMovie) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: HostedMovieDao, entities: List<HostedMovie>) {
        dao.insert(entities)
    }
}