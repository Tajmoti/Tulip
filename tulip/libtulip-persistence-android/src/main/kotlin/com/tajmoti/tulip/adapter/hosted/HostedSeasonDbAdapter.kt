package com.tajmoti.tulip.adapter.hosted

import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.hosted.HostedSeasonDao
import com.tajmoti.tulip.entity.hosted.HostedSeason
import kotlinx.coroutines.flow.Flow

class HostedSeasonDbAdapter : DbAdapter<HostedSeasonDao, SeasonKey.Hosted, HostedSeason> {

    override fun findByKeyFromDb(dao: HostedSeasonDao, key: SeasonKey.Hosted): Flow<HostedSeason?> = with(key) {
        return dao.getBySeasonNumber(streamingService, tvShowKey.id, seasonNumber)
    }

    fun findByTvShowKeyFromDb(dao: HostedSeasonDao, key: TvShowKey.Hosted): Flow<List<HostedSeason>> = with(key) {
        return dao.getForShow(streamingService, id)
    }

    override suspend fun insertToDb(dao: HostedSeasonDao, entity: HostedSeason) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: HostedSeasonDao, entities: List<HostedSeason>) {
        dao.insert(entities)
    }
}