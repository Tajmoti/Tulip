package com.tajmoti.tulip.adapter.tmdb

import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbSeasonDao
import com.tajmoti.tulip.entity.tmdb.TmdbSeason
import kotlinx.coroutines.flow.Flow

class TmdbSeasonDbAdapter : DbAdapter<TmdbSeasonDao, SeasonKey.Tmdb, TmdbSeason> {

    override fun findByKeyFromDb(dao: TmdbSeasonDao, key: SeasonKey.Tmdb): Flow<TmdbSeason?> {
        return dao.getSeason(key.tvShowKey.id, key.seasonNumber)
    }

    fun findByTvShowKeyFromDb(dao: TmdbSeasonDao, key: TvShowKey.Tmdb): Flow<List<TmdbSeason>> = with(key) {
        return dao.getSeasons(id)
    }

    override suspend fun insertToDb(dao: TmdbSeasonDao, entity: TmdbSeason) {
        dao.insertSeason(entity)
    }

    override suspend fun insertToDb(dao: TmdbSeasonDao, entities: List<TmdbSeason>) {
        dao.insertSeasons(entities)
    }
}