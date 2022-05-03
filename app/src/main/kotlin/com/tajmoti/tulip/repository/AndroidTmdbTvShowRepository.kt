package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.repository.TmdbTvShowRepository
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.adapter.TmdbSeasonDbAdapter
import com.tajmoti.tulip.db.adapter.TmdbTvShowDbAdapter
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.mapper.AndroidTmdbSeasonMapper
import com.tajmoti.tulip.mapper.AndroidTmdbTvShowMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AndroidTmdbTvShowRepository @Inject constructor(
    private val dao: TmdbDao,
) : TmdbTvShowRepository {
    private val tvShowAdapter = TmdbTvShowDbAdapter()
    private val seasonAdapter = TmdbSeasonDbAdapter()
    private val tvShowMapper = AndroidTmdbTvShowMapper()
    private val seasonMapper = AndroidTmdbSeasonMapper()

    override fun findByKey(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?> {
        val tvShowFlow = tvShowAdapter.findByKeyFromDb(dao, key)
        val seasonsFlow = seasonAdapter.findByTvShowKeyFromDb(dao, key)
        return combine(tvShowFlow, seasonsFlow) { item, seasons ->
            val mappedSeasons = seasons.map { season -> seasonMapper.fromDb(season) }
            item?.let { tvShowMapper.fromDb(it, mappedSeasons) }
        }
    }

    override suspend fun insert(repo: TvShow.Tmdb) {
        tvShowAdapter.insertToDb(dao, tvShowMapper.toDb(repo))
        seasonAdapter.insertToDb(dao, repo.seasons.map { season -> seasonMapper.toDb(season) })
    }
}
