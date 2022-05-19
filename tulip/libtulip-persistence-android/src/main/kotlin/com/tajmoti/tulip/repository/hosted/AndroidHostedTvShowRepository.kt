package com.tajmoti.tulip.repository.hosted

import com.tajmoti.libtulip.model.Season
import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.HostedTvShowRepository
import com.tajmoti.tulip.adapter.hosted.HostedSeasonDbAdapter
import com.tajmoti.tulip.adapter.hosted.HostedTvShowDbAdapter
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.dao.hosted.HostedSeasonDao
import com.tajmoti.tulip.dao.hosted.HostedTvShowDao
import com.tajmoti.tulip.mapper.hosted.HostedSeasonMapper
import com.tajmoti.tulip.mapper.hosted.HostedTvShowMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidHostedTvShowRepository @Inject constructor(
    private val hostedTvShowDao: HostedTvShowDao,
    private val hostedSeasonDao: HostedSeasonDao,
    private val itemMappingDao: ItemMappingDao,
) : HostedTvShowRepository {
    private val tvShowMapper = HostedTvShowMapper()
    private val seasonMapper = HostedSeasonMapper()
    private val tvShowAdapter = HostedTvShowDbAdapter()
    private val seasonAdapter = HostedSeasonDbAdapter()

    override fun findByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?> {
        val tmdbMappingFlow = itemMappingDao.getTmdbIdByHostedKey(key.streamingService, key.id)
        val tvShowFlow = hostedTvShowDao.getByKey(key.streamingService, key.id)
        val seasonsFlow = getSeasonsByTvShow(key)
        return combine(tmdbMappingFlow, tvShowFlow, seasonsFlow) { tmdbId, item, seasons ->
            val tmdbKey = tmdbId?.let { TvShowKey.Tmdb(it.tmdbId) }
            item?.let { tvShowMapper.fromDb(it, tmdbKey, seasons) }
        }
    }

    private fun getSeasonsByTvShow(key: TvShowKey.Hosted): Flow<List<Season.Hosted>> {
        return seasonAdapter.findByTvShowKeyFromDb(hostedSeasonDao, key)
            .map { seasons -> seasons.map { season -> seasonMapper.fromDb(season) } }
    }

    override suspend fun insert(repo: TvShow.Hosted) {
        tvShowAdapter.insertToDb(hostedTvShowDao, tvShowMapper.toDb(repo))
        seasonAdapter.insertToDb(hostedSeasonDao, repo.seasons.map { seasonMapper.toDb(it) })
    }
}