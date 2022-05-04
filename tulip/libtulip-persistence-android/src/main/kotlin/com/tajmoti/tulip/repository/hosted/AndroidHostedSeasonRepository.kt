package com.tajmoti.tulip.repository.hosted

import com.tajmoti.libtulip.repository.HostedSeasonRepository
import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.adapter.hosted.HostedEpisodeDbAdapter
import com.tajmoti.tulip.adapter.hosted.HostedSeasonDbAdapter
import com.tajmoti.tulip.dao.hosted.HostedEpisodeDao
import com.tajmoti.tulip.dao.hosted.HostedSeasonDao
import com.tajmoti.tulip.mapper.hosted.HostedEpisodeMapper
import com.tajmoti.tulip.mapper.hosted.HostedSeasonMapper
import com.tajmoti.tulip.mapper.hosted.HostedSeasonWithEpisodesMapper
import com.tajmoti.tulip.repository.RwRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedSeasonRepository @Inject constructor(
    private val dao: HostedSeasonDao,
    private val hostedEpisodeDao: HostedEpisodeDao,
) : HostedSeasonRepository, RwRepository<Season.Hosted, SeasonKey.Hosted> by RwRepositoryImpl(
    dao = dao,
    adapter = HostedSeasonDbAdapter(),
    mapper = HostedSeasonMapper()
) {
    private val adapter = HostedSeasonDbAdapter()
    private val mapper = HostedSeasonMapper()
    private val seasonWithEpisodeMapper = HostedSeasonWithEpisodesMapper()
    private val episodeAdapter = HostedEpisodeDbAdapter()
    private val episodeMapper = HostedEpisodeMapper()

    override fun findSeasonWithEpisodesByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?> = with(key) {
        val tvShowFlow = dao.getBySeasonNumber(streamingService, tvShowKey.id, seasonNumber)
        val seasonsFlow = getEpisodesBySeason(key)
        return combine(tvShowFlow, seasonsFlow) { item, episodes ->
            if (item == null || episodes == null) return@combine null
            seasonWithEpisodeMapper.fromDb(item, episodes)
        }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Hosted): Flow<List<Episode.Hosted>?> {
        return episodeAdapter.findBySeasonKeyFromDb(hostedEpisodeDao, key)
            .map { seasons -> seasons.map { season -> episodeMapper.fromDb(season) } }
            .map { it.takeUnless { it.isEmpty() } }
    }

    override suspend fun insertSeasonWithEpisodes(season: Season.Hosted, episodes: List<Episode.Hosted>) {
        adapter.insertToDb(dao, mapper.toDb(season))
        episodeAdapter.insertToDb(hostedEpisodeDao, episodes.map { episodeMapper.toDb(it) })
    }
}
