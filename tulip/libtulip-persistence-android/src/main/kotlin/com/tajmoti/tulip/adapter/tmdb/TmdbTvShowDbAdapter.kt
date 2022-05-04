package com.tajmoti.tulip.adapter.tmdb

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.dao.tmdb.TmdbTvShowDao
import com.tajmoti.tulip.entity.tmdb.TmdbTvShow
import kotlinx.coroutines.flow.Flow

class TmdbTvShowDbAdapter : DbAdapter<TmdbTvShowDao, TvShowKey.Tmdb, TmdbTvShow> {

    override fun findByKeyFromDb(dao: TmdbTvShowDao, key: TvShowKey.Tmdb): Flow<TmdbTvShow?> {
        return dao.getTvShow(key.id)
    }

    override suspend fun insertToDb(dao: TmdbTvShowDao, entity: TmdbTvShow) {
        dao.insertTvShow(entity)
    }

    override suspend fun insertToDb(dao: TmdbTvShowDao, entities: List<TmdbTvShow>) {
        dao.insertTvShows(entities)
    }
}