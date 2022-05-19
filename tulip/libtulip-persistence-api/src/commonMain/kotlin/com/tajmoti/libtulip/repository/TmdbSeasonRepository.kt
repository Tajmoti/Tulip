package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.Season
import com.tajmoti.libtulip.model.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow

interface TmdbSeasonRepository : RwRepository<Season.Tmdb, SeasonKey.Tmdb> {

    fun findSeasonWithEpisodesByKey(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?>

    suspend fun insertSeasonWithEpisodes(season: Season.Tmdb, episodes: List<Episode.Tmdb>)
}
