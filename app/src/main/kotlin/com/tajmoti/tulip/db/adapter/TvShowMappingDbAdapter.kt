package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.dao.hosted.TmdbMappingDao
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping
import kotlinx.coroutines.flow.Flow

class TvShowMappingDbAdapter : DbAdapter<TmdbMappingDao, TvShowKey.Hosted, DbTmdbMapping> {

    override fun findByKeyFromDb(dao: TmdbMappingDao, key: TvShowKey.Hosted): Flow<DbTmdbMapping?> {
        return dao.getTmdbIdByHostedKey(key.streamingService, key.id)
    }

    fun findByKeyFromByTmdbKey(dao: TmdbMappingDao, key: TvShowKey.Tmdb): Flow<List<DbTmdbMapping>> {
        return dao.getHostedKeysByTmdbId(key.id)
    }

    override suspend fun insertToDb(dao: TmdbMappingDao, entity: DbTmdbMapping) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: TmdbMappingDao, entities: List<DbTmdbMapping>) {
        dao.insert(entities)
    }
}