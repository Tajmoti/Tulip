package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BrowserTmdbSeasonRepository(private val episodeRepository: TmdbEpisodeRepository) : TmdbSeasonRepository {
    private val seasonStorage = BrowserStorage<SeasonKey.Tmdb, Season.Tmdb>()

    override fun findByKey(key: SeasonKey.Tmdb): Flow<Season.Tmdb?> {
        return seasonStorage.get(key)
    }

    override suspend fun insert(repo: Season.Tmdb) {
        seasonStorage.put(repo.key, repo)
    }


    override fun findSeasonWithEpisodesByKey(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?> = with(key) {
        val tvShowFlow = findByKey(key)
        val seasonsFlow = getEpisodesBySeason(key)
        return combine(tvShowFlow, seasonsFlow) { item, episodes ->
            item?.let { SeasonWithEpisodes.Tmdb(item, episodes) }
        }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>> {
        return episodeRepository.findBySeason(key)
    }

    override suspend fun insertSeasonWithEpisodes(season: Season.Tmdb, episodes: List<Episode.Tmdb>) {
        insert(season)
        episodes.forEach { episode -> episodeRepository.insert(episode) }
    }
}