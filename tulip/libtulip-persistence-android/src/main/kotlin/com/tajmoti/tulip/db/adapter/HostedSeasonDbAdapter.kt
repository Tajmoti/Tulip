package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.entity.hosted.DbSeason
import kotlinx.coroutines.flow.Flow

class HostedSeasonDbAdapter : DbAdapter<SeasonDao, SeasonKey.Hosted, DbSeason> {

    override fun findByKeyFromDb(dao: SeasonDao, key: SeasonKey.Hosted): Flow<DbSeason?> = with(key) {
        return dao.getBySeasonNumber(streamingService, tvShowKey.id, seasonNumber)
    }

    fun findByTvShowKeyFromDb(dao: SeasonDao, key: TvShowKey.Hosted): Flow<List<DbSeason>> = with(key) {
        return dao.getForShow(streamingService, id)
    }

    override suspend fun insertToDb(dao: SeasonDao, entity: DbSeason) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: SeasonDao, entities: List<DbSeason>) {
        dao.insert(entities)
    }
}