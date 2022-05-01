package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BrowserHostedSeasonRepository(private val episodeRepository: HostedEpisodeRepository) : HostedSeasonRepository {
    private val seasonStorage = BrowserStorage<SeasonKey.Hosted, Season.Hosted>()

    override fun findByKey(key: SeasonKey.Hosted): Flow<Season.Hosted?> {
        return seasonStorage.get(key)
    }

    override suspend fun insert(repo: Season.Hosted) {
        seasonStorage.put(repo.key, repo)
    }


    override fun findSeasonWithEpisodesByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?> = with(key) {
        val tvShowFlow = findByKey(key)
        val seasonsFlow = getEpisodesBySeason(key)
        return combine(tvShowFlow, seasonsFlow) { item, episodes ->
            item?.let { SeasonWithEpisodes.Hosted(item, episodes) }
        }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Hosted): Flow<List<Episode.Hosted>> {
        return episodeRepository.findBySeason(key)
    }

    override suspend fun insertSeasonWithEpisodes(season: Season.Hosted, episodes: List<Episode.Hosted>) {
        insert(season)
        episodes.forEach { episode -> episodeRepository.insert(episode) }
    }
}