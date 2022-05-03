package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.entity.hosted.DbEpisode
import kotlinx.coroutines.flow.Flow

class HostedEpisodeDbAdapter : DbAdapter<EpisodeDao, EpisodeKey.Hosted, DbEpisode> {

    override fun findByKeyFromDb(dao: EpisodeDao, key: EpisodeKey.Hosted): Flow<DbEpisode?> {
        return dao.getByKey(key.streamingService, key.seasonKey.tvShowKey.id, key.seasonKey.seasonNumber, key.id)
    }

    fun findBySeasonKeyFromDb(dao: EpisodeDao, key: SeasonKey.Hosted): Flow<List<DbEpisode>> = with(key) {
        return dao.getForSeason(streamingService, tvShowKey.id, seasonNumber)
    }

    override suspend fun insertToDb(dao: EpisodeDao, entity: DbEpisode) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: EpisodeDao, entities: List<DbEpisode>) {
        dao.insert(entities)
    }
}