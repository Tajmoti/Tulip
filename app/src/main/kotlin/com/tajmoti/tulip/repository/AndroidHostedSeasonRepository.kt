package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.HostedSeasonRepository
import com.tajmoti.libtulip.data.RwRepository
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.streamingService
import com.tajmoti.tulip.db.adapter.HostedEpisodeDbAdapter
import com.tajmoti.tulip.db.adapter.HostedSeasonDbAdapter
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.mapper.AndroidHostedEpisodeMapper
import com.tajmoti.tulip.mapper.AndroidHostedSeasonMapper
import com.tajmoti.tulip.mapper.AndroidHostedSeasonWithEpisodesMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedSeasonRepository @Inject constructor(
    private val dao: SeasonDao,
    private val episodeDao: EpisodeDao,
) : HostedSeasonRepository, RwRepository<Season.Hosted, SeasonKey.Hosted> by RwRepositoryImpl(
    dao = dao,
    adapter = HostedSeasonDbAdapter(),
    mapper = AndroidHostedSeasonMapper()
) {
    private val adapter = HostedSeasonDbAdapter()
    private val mapper = AndroidHostedSeasonMapper()
    private val seasonWithEpisodeMapper = AndroidHostedSeasonWithEpisodesMapper()
    private val episodeAdapter = HostedEpisodeDbAdapter()
    private val episodeMapper = AndroidHostedEpisodeMapper()

    override fun findSeasonWithEpisodesByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?> = with(key) {
        val tvShowFlow = dao.getBySeasonNumber(streamingService, tvShowKey.id, seasonNumber)
        val seasonsFlow = getEpisodesBySeason(key)
        return combine(tvShowFlow, seasonsFlow) { item, episodes ->
            item?.let { seasonWithEpisodeMapper.fromDb(it, episodes) }
        }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Hosted): Flow<List<Episode.Hosted>> {
        return episodeAdapter.findBySeasonKeyFromDb(episodeDao, key)
            .map { seasons -> seasons.map { season -> episodeMapper.fromDb(season) } }
    }

    override suspend fun insertSeasonWithEpisodes(season: Season.Hosted, episodes: List<Episode.Hosted>) {
        adapter.insertToDb(dao, mapper.toDb(season))
        episodeAdapter.insertToDb(episodeDao, episodes.map { episodeMapper.toDb(it) })
    }
}
