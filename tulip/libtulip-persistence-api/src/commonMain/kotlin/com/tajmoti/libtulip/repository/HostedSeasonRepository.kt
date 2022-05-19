package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.Season
import com.tajmoti.libtulip.model.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow

interface HostedSeasonRepository : RwRepository<Season.Hosted, SeasonKey.Hosted> {

    fun findSeasonWithEpisodesByKey(key: SeasonKey.Hosted): Flow<SeasonWithEpisodes.Hosted?>

    suspend fun insertSeasonWithEpisodes(season: Season.Hosted, episodes: List<Episode.Hosted>)
}
