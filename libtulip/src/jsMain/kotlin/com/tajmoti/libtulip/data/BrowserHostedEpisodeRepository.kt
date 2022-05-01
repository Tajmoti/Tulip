package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserHostedEpisodeRepository : HostedEpisodeRepository {
    private val episodeStorage = BrowserStorage<EpisodeKey.Hosted, Episode.Hosted>()

    override fun findByKey(key: EpisodeKey.Hosted): Flow<Episode.Hosted?> {
        return episodeStorage.get(key)
    }

    override suspend fun insert(repo: Episode.Hosted) {
        episodeStorage.put(repo.key, repo)
    }

    override fun findBySeason(seasonKey: SeasonKey.Hosted): Flow<List<Episode.Hosted>> {
        return episodeStorage.getAll()
            .map {
                it.filter { episode -> episode.key.seasonKey == seasonKey }
                    .sortedBy { episode -> episode.episodeNumber }
            }
    }
}