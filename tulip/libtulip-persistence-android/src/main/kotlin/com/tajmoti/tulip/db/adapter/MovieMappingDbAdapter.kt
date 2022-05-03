package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.dao.hosted.TmdbMappingDao
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping
import kotlinx.coroutines.flow.Flow

class MovieMappingDbAdapter : DbAdapter<TmdbMappingDao, MovieKey.Hosted, DbTmdbMapping> {

    override fun findByKeyFromDb(dao: TmdbMappingDao, key: MovieKey.Hosted): Flow<DbTmdbMapping?> {
        return dao.getTmdbIdByHostedKey(key.streamingService, key.id)
    }

    fun findByKeyFromByTmdbKey(dao: TmdbMappingDao, key: MovieKey.Tmdb): Flow<List<DbTmdbMapping>> {
        return dao.getHostedKeysByTmdbId(key.id)
    }

    override suspend fun insertToDb(dao: TmdbMappingDao, entity: DbTmdbMapping) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: TmdbMappingDao, entities: List<DbTmdbMapping>) {
        dao.insert(entities)
    }
}