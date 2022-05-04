package com.tajmoti.tulip.adapter.hosted

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.hosted.HostedTvShowDao
import com.tajmoti.tulip.entity.hosted.HostedTvShow
import kotlinx.coroutines.flow.Flow

class HostedTvShowDbAdapter : DbAdapter<HostedTvShowDao, TvShowKey.Hosted, HostedTvShow> {

    override fun findByKeyFromDb(dao: HostedTvShowDao, key: TvShowKey.Hosted): Flow<HostedTvShow?> {
        return dao.getByKey(key.streamingService, key.id)
    }

    override suspend fun insertToDb(dao: HostedTvShowDao, entity: HostedTvShow) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: HostedTvShowDao, entities: List<HostedTvShow>) {
        dao.insert(entities)
    }
}