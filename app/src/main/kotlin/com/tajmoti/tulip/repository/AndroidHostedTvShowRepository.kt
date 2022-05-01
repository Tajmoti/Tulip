package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.HostedTvShowRepository
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.adapter.HostedSeasonDbAdapter
import com.tajmoti.tulip.db.adapter.HostedTvShowDbAdapter
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.dao.hosted.TmdbMappingDao
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import com.tajmoti.tulip.mapper.AndroidHostedSeasonMapper
import com.tajmoti.tulip.mapper.AndroidHostedTvShowMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedTvShowRepository @Inject constructor(
    private val tvShowDao: TvShowDao,
    private val seasonDao: SeasonDao,
    private val tmdbMappingDao: TmdbMappingDao,
) : HostedTvShowRepository {
    private val tvShowMapper = AndroidHostedTvShowMapper()
    private val seasonMapper = AndroidHostedSeasonMapper()
    private val tvShowAdapter = HostedTvShowDbAdapter()
    private val seasonAdapter = HostedSeasonDbAdapter()

    override fun findByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?> {
        val tmdbMappingFlow = tmdbMappingDao.getTmdbIdByHostedKey(key.streamingService, key.id)
        val tvShowFlow = tvShowDao.getByKey(key.streamingService, key.id)
        val seasonsFlow = getSeasonsByTvShow(key)
        return combine(tmdbMappingFlow, tvShowFlow, seasonsFlow) { tmdbId, item, seasons ->
            val tmdbKey = tmdbId?.let { TvShowKey.Tmdb(it.tmdbId) }
            item?.let { tvShowMapper.fromDb(it, tmdbKey, seasons) }
        }
    }

    private fun getSeasonsByTvShow(key: TvShowKey.Hosted): Flow<List<Season.Hosted>> {
        return seasonAdapter.findByTvShowKeyFromDb(seasonDao, key)
            .map { seasons -> seasons.map { season -> seasonMapper.fromDb(season) } }
    }

    override suspend fun insert(repo: TvShow.Hosted) {
        tvShowAdapter.insertToDb(tvShowDao, tvShowMapper.toDb(repo))
        seasonAdapter.insertToDb(seasonDao, repo.seasons.map { seasonMapper.toDb(it) })
    }
}