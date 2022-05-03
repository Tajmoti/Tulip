package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import com.tajmoti.tulip.db.entity.hosted.DbTvShow
import kotlinx.coroutines.flow.Flow

class HostedTvShowDbAdapter : DbAdapter<TvShowDao, TvShowKey.Hosted, DbTvShow> {

    override fun findByKeyFromDb(dao: TvShowDao, key: TvShowKey.Hosted): Flow<DbTvShow?> {
        return dao.getByKey(key.streamingService, key.id)
    }

    override suspend fun insertToDb(dao: TvShowDao, entity: DbTvShow) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: TvShowDao, entities: List<DbTvShow>) {
        dao.insert(entities)
    }
}