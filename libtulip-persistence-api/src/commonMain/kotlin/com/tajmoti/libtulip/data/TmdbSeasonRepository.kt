package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow

interface TmdbSeasonRepository : RwRepository<Season.Tmdb, SeasonKey.Tmdb> {

    fun findSeasonWithEpisodesByKey(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?>

    suspend fun insertSeasonWithEpisodes(season: Season.Tmdb, episodes: List<Episode.Tmdb>)
}
