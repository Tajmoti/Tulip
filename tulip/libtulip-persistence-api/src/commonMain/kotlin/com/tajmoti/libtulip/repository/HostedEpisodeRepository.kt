package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow

interface HostedEpisodeRepository : RwRepository<Episode.Hosted, EpisodeKey.Hosted> {

    fun findBySeason(seasonKey: SeasonKey.Hosted): Flow<List<Episode.Hosted>>
}
