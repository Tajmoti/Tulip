package com.tajmoti.tulip.repository.tmdb

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.libtulip.repository.TmdbEpisodeRepository
import com.tajmoti.tulip.adapter.tmdb.TmdbEpisodeDbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbEpisodeDao
import com.tajmoti.tulip.mapper.tmdb.TmdbEpisodeMapper
import com.tajmoti.tulip.repository.RwRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidTmdbEpisodeRepository @Inject constructor(
    private val dao: TmdbEpisodeDao
) : TmdbEpisodeRepository, RwRepository<Episode.Tmdb, EpisodeKey.Tmdb> by RwRepositoryImpl(
    dao = dao,
    adapter = TmdbEpisodeDbAdapter(),
    mapper = TmdbEpisodeMapper()
) {
    private val adapter = TmdbEpisodeDbAdapter()
    private val mapper = TmdbEpisodeMapper()

    override fun findBySeason(seasonKey: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>> {
        return adapter.findBySeasonKeyFromDb(dao, seasonKey)
            .map { dbEpisodes -> dbEpisodes.map { dbEpisode -> mapper.fromDb(dbEpisode) } }
    }
}