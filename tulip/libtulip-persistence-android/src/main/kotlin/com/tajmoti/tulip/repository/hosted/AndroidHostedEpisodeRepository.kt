package com.tajmoti.tulip.repository.hosted

import com.tajmoti.libtulip.repository.HostedEpisodeRepository
import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.adapter.hosted.HostedEpisodeDbAdapter
import com.tajmoti.tulip.dao.hosted.HostedEpisodeDao
import com.tajmoti.tulip.mapper.hosted.HostedEpisodeMapper
import com.tajmoti.tulip.repository.RwRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedEpisodeRepository @Inject constructor(
    private val dao: HostedEpisodeDao
) : HostedEpisodeRepository, RwRepository<Episode.Hosted, EpisodeKey.Hosted> by RwRepositoryImpl(
    dao = dao,
    adapter = HostedEpisodeDbAdapter(),
    mapper = HostedEpisodeMapper()
) {
    private val adapter = HostedEpisodeDbAdapter()
    private val mapper = HostedEpisodeMapper()

    override fun findBySeason(seasonKey: SeasonKey.Hosted): Flow<List<Episode.Hosted>> {
        return adapter.findBySeasonKeyFromDb(dao, seasonKey)
            .map { dbEpisodes -> dbEpisodes.map { dbEpisode -> mapper.fromDb(dbEpisode) } }
    }
}