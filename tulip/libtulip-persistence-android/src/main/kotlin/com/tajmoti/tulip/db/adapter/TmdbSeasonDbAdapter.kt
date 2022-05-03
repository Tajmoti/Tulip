package com.tajmoti.tulip.db.adapter

import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import kotlinx.coroutines.flow.Flow

class TmdbSeasonDbAdapter : DbAdapter<TmdbDao, SeasonKey.Tmdb, DbTmdbSeason> {

    override fun findByKeyFromDb(dao: TmdbDao, key: SeasonKey.Tmdb): Flow<DbTmdbSeason?> {
        return dao.getSeason(key.tvShowKey.id, key.seasonNumber)
    }

    fun findByTvShowKeyFromDb(dao: TmdbDao, key: TvShowKey.Tmdb): Flow<List<DbTmdbSeason>> = with(key) {
        return dao.getSeasons(id)
    }

    override suspend fun insertToDb(dao: TmdbDao, entity: DbTmdbSeason) {
        dao.insertSeason(entity)
    }

    override suspend fun insertToDb(dao: TmdbDao, entities: List<DbTmdbSeason>) {
        dao.insertSeasons(entities)
    }
}