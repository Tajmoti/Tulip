package com.tajmoti.tulip.adapter.hosted

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.hosted.HostedEpisodeDao
import com.tajmoti.tulip.entity.hosted.HostedEpisode
import kotlinx.coroutines.flow.Flow

class HostedEpisodeDbAdapter : DbAdapter<HostedEpisodeDao, EpisodeKey.Hosted, HostedEpisode> {

    override fun findByKeyFromDb(dao: HostedEpisodeDao, key: EpisodeKey.Hosted): Flow<HostedEpisode?> {
        return dao.getByKey(key.streamingService, key.seasonKey.tvShowKey.id, key.seasonKey.seasonNumber, key.id)
    }

    fun findBySeasonKeyFromDb(dao: HostedEpisodeDao, key: SeasonKey.Hosted): Flow<List<HostedEpisode>> = with(key) {
        return dao.getForSeason(streamingService, tvShowKey.id, seasonNumber)
    }

    override suspend fun insertToDb(dao: HostedEpisodeDao, entity: HostedEpisode) {
        dao.insert(entity)
    }

    override suspend fun insertToDb(dao: HostedEpisodeDao, entities: List<HostedEpisode>) {
        dao.insert(entities)
    }
}