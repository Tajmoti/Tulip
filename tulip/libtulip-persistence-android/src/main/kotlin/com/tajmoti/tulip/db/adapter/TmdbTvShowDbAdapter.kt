package com.tajmoti.tulip.db.adapter

import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbTv
import kotlinx.coroutines.flow.Flow

class TmdbTvShowDbAdapter : DbAdapter<TmdbDao, TvShowKey.Tmdb, DbTmdbTv> {

    override fun findByKeyFromDb(dao: TmdbDao, key: TvShowKey.Tmdb): Flow<DbTmdbTv?> {
        return dao.getTv(key.id).mapNotNulls { it.tvShow }
    }

    override suspend fun insertToDb(dao: TmdbDao, entity: DbTmdbTv) {
        dao.insertTv(entity)
    }

    override suspend fun insertToDb(dao: TmdbDao, entities: List<DbTmdbTv>) {
        dao.insertTv(entities)
    }
}