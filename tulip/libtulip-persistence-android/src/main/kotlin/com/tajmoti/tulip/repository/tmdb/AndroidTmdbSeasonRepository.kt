package com.tajmoti.tulip.repository.tmdb

import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.Season
import com.tajmoti.libtulip.model.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.libtulip.repository.TmdbSeasonRepository
import com.tajmoti.tulip.adapter.tmdb.TmdbEpisodeDbAdapter
import com.tajmoti.tulip.adapter.tmdb.TmdbSeasonDbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbEpisodeDao
import com.tajmoti.tulip.dao.tmdb.TmdbSeasonDao
import com.tajmoti.tulip.mapper.tmdb.TmdbEpisodeMapper
import com.tajmoti.tulip.mapper.tmdb.TmdbSeasonMapper
import com.tajmoti.tulip.mapper.tmdb.TmdbSeasonWithEpisodesMapper
import com.tajmoti.tulip.repository.RwRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidTmdbSeasonRepository @Inject constructor(
    private val seasonDao: TmdbSeasonDao,
    private val episodeDao: TmdbEpisodeDao,
) : TmdbSeasonRepository, RwRepository<Season.Tmdb, SeasonKey.Tmdb> by RwRepositoryImpl(
    dao = seasonDao,
    adapter = TmdbSeasonDbAdapter(),
    mapper = TmdbSeasonMapper()
) {
    private val adapter = TmdbSeasonDbAdapter()
    private val mapper = TmdbSeasonMapper()
    private val seasonWithEpisodeMapper = TmdbSeasonWithEpisodesMapper()
    private val episodeAdapter = TmdbEpisodeDbAdapter()
    private val episodeMapper = TmdbEpisodeMapper()

    override fun findSeasonWithEpisodesByKey(key: SeasonKey.Tmdb): Flow<SeasonWithEpisodes.Tmdb?> = with(key) {
        val tvShowFlow = seasonDao.getSeason(tvShowKey.id, seasonNumber)
        val seasonsFlow = getEpisodesBySeason(key)
        return combine(tvShowFlow, seasonsFlow) { item, episodes ->
            if (item == null || episodes == null) return@combine null
            seasonWithEpisodeMapper.fromDb(item, episodes)
        }
    }

    private fun getEpisodesBySeason(key: SeasonKey.Tmdb): Flow<List<Episode.Tmdb>?> {
        return episodeAdapter.findBySeasonKeyFromDb(episodeDao, key)
            .map { seasons -> seasons.map { season -> episodeMapper.fromDb(season) } }
            .map { it.takeUnless { it.isEmpty() } }
    }

    override suspend fun insertSeasonWithEpisodes(season: Season.Tmdb, episodes: List<Episode.Tmdb>) {
        adapter.insertToDb(seasonDao, mapper.toDb(season))
        episodeAdapter.insertToDb(episodeDao, episodes.map { episodeMapper.toDb(it) })
    }
}