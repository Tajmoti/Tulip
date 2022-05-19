package com.tajmoti.tulip.repository.tmdb

import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvShowRepository
import com.tajmoti.tulip.adapter.tmdb.TmdbSeasonDbAdapter
import com.tajmoti.tulip.adapter.tmdb.TmdbTvShowDbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbSeasonDao
import com.tajmoti.tulip.dao.tmdb.TmdbTvShowDao
import com.tajmoti.tulip.mapper.tmdb.TmdbSeasonMapper
import com.tajmoti.tulip.mapper.tmdb.TmdbTvShowMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AndroidTmdbTvShowRepository @Inject constructor(
    private val dao: TmdbTvShowDao,
    private val seasonDao: TmdbSeasonDao,
) : TmdbTvShowRepository {
    private val tvShowAdapter = TmdbTvShowDbAdapter()
    private val seasonAdapter = TmdbSeasonDbAdapter()
    private val tvShowMapper = TmdbTvShowMapper()
    private val seasonMapper = TmdbSeasonMapper()

    override fun findByKey(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?> {
        val tvShowFlow = tvShowAdapter.findByKeyFromDb(dao, key)
        val seasonsFlow = seasonAdapter.findByTvShowKeyFromDb(seasonDao, key)
        return combine(tvShowFlow, seasonsFlow) { item, seasons ->
            val mappedSeasons = seasons.map { season -> seasonMapper.fromDb(season) }
            item?.let { tvShowMapper.fromDb(it, mappedSeasons) }
        }
    }

    override suspend fun insert(repo: TvShow.Tmdb) {
        tvShowAdapter.insertToDb(dao, tvShowMapper.toDb(repo))
        seasonAdapter.insertToDb(seasonDao, repo.seasons.map { season -> seasonMapper.toDb(season) })
    }
}
