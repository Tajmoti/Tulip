package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.HostedEpisodeRepository
import com.tajmoti.libtulip.data.RwRepository
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.db.adapter.HostedEpisodeDbAdapter
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.mapper.AndroidHostedEpisodeMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedEpisodeRepository @Inject constructor(
    private val dao: EpisodeDao
) : HostedEpisodeRepository, RwRepository<Episode.Hosted, EpisodeKey.Hosted> by RwRepositoryImpl(
    dao = dao,
    adapter = HostedEpisodeDbAdapter(),
    mapper = AndroidHostedEpisodeMapper()
) {
    private val adapter = HostedEpisodeDbAdapter()
    private val mapper = AndroidHostedEpisodeMapper()

    override fun findBySeason(seasonKey: SeasonKey.Hosted): Flow<List<Episode.Hosted>> {
        return adapter.findBySeasonKeyFromDb(dao, seasonKey)
            .map { dbEpisodes -> dbEpisodes.map { dbEpisode -> mapper.fromDb(dbEpisode) } }
    }
}