package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.Flow

interface TmdbEpisodeRepository : RwRepository<Episode.Tmdb, EpisodeKey.Tmdb> {

    fun findBySeason(seasonKey: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>>
}
