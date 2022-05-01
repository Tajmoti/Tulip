package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserTmdbEpisodeRepository : TmdbEpisodeRepository {
    private val episodeStorage = BrowserStorage<EpisodeKey.Tmdb, Episode.Tmdb>()

    override fun findByKey(key: EpisodeKey.Tmdb): Flow<Episode.Tmdb?> {
        return episodeStorage.get(key)
    }

    override suspend fun insert(repo: Episode.Tmdb) {
        episodeStorage.put(repo.key, repo)
    }

    override fun findBySeason(seasonKey: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>> {
        return episodeStorage.getAll()
            .map { it.filter { episode -> episode.key.seasonKey == seasonKey } }
    }
}