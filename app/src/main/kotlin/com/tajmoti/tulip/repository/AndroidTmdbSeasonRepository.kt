package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.RwRepository
import com.tajmoti.libtulip.data.TmdbSeasonRepository
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.db.adapter.TmdbEpisodeDbAdapter
import com.tajmoti.tulip.db.adapter.TmdbSeasonDbAdapter
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.mapper.AndroidTmdbEpisodeMapper
import com.tajmoti.tulip.mapper.AndroidTmdbSeasonMapper
import com.tajmoti.tulip.mapper.AndroidTmdbSeasonWithEpisodesMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidTmdbSeasonRepository @Inject constructor(
    private val dao: TmdbDao,
) : TmdbSeasonRepository, RwRepository<Season.Tmdb, SeasonKey.Tmdb> by RwRepositoryImpl(
    dao = dao,
    adapter = TmdbSeasonDbAdapter(),
    mapper = AndroidTmdbSeasonMapper()
) {
    private val adapter = TmdbSeasonDbAdapter()
    private val mapper = AndroidTmdbSeasonMapper()
    private val seasonWithEpisodeMapper = AndroidTmdbSeasonWithEpisodesMapper()
    private val episodeAdapter = TmdbEpisodeDbAdapter()
    private val episodeMapper = AndroidTmdbEpisodeMapper()

    override fun findSeasonWithEpisodesByKey(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?> = with(key) {
        val tvShowFlow = dao.getSeason(tvShowKey.id, seasonNumber)
        val seasonsFlow = getEpisodesBySeason(key)
        return combine(tvShowFlow, seasonsFlow) { item, episodes ->
            item?.let { seasonWithEpisodeMapper.fromDb(it, episodes) }
        }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>> {
        return episodeAdapter.findBySeasonKeyFromDb(dao, key)
            .map { seasons -> seasons.map { season -> episodeMapper.fromDb(season) } }
    }

    override suspend fun insertSeasonWithEpisodes(season: Season.Tmdb, episodes: List<Episode.Tmdb>) {
        adapter.insertToDb(dao, mapper.toDb(season))
        episodeAdapter.insertToDb(dao, episodes.map { episodeMapper.toDb(it) })
    }
}