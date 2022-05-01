package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.entity.hosted.DbMovie
import kotlinx.coroutines.flow.Flow

class HostedMovieDbAdapter : DbAdapter<MovieDao, MovieKey.Hosted, DbMovie> {

    override fun findByKeyFromDb(dao: MovieDao, key: MovieKey.Hosted): Flow<DbMovie?> {
        return dao.getByKey(key.streamingService, key.id)
    }

    override suspend fun insertToDb(dao: MovieDao, entity: DbMovie) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: MovieDao, entities: List<DbMovie>) {
        dao.insert(entities)
    }
}