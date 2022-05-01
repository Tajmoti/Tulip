package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.TvShowMappingRepository
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.adapter.TvShowMappingDbAdapter
import com.tajmoti.tulip.db.dao.hosted.TmdbMappingDao
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping
import com.tajmoti.tulip.mapper.AndroidTvShowMappingMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidTvShowMappingRepository @Inject constructor(
    private val dao: TmdbMappingDao
) : TvShowMappingRepository {
    private val adapter = TvShowMappingDbAdapter()
    private val mapper = AndroidTvShowMappingMapper()

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        adapter.insertToDb(dao, DbTmdbMapping(hosted.streamingService, hosted.id, tmdb.id))
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return adapter.findByKeyFromByTmdbKey(dao, tmdb)
            .map { mappings -> mappings.map { mapping -> mapper.fromDb(mapping) }.toSet() }
    }
}