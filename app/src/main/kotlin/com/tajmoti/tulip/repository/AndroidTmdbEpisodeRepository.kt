package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.RwRepository
import com.tajmoti.libtulip.data.TmdbEpisodeRepository
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.db.adapter.TmdbEpisodeDbAdapter
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.mapper.AndroidTmdbEpisodeMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidTmdbEpisodeRepository @Inject constructor(
    private val dao: TmdbDao
) : TmdbEpisodeRepository, RwRepository<Episode.Tmdb, EpisodeKey.Tmdb> by RwRepositoryImpl(
    dao = dao,
    adapter = TmdbEpisodeDbAdapter(),
    mapper = AndroidTmdbEpisodeMapper()
) {
    private val adapter = TmdbEpisodeDbAdapter()
    private val mapper = AndroidTmdbEpisodeMapper()

    override fun findBySeason(seasonKey: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>> {
        return adapter.findBySeasonKeyFromDb(dao, seasonKey)
            .map { dbEpisodes -> dbEpisodes.map { dbEpisode -> mapper.fromDb(dbEpisode) } }
    }
}