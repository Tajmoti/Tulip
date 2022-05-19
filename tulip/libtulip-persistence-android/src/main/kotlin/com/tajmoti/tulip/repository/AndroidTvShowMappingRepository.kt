package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TvShowMappingRepository
import com.tajmoti.tulip.adapter.TvShowMappingDbAdapter
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.entity.ItemMapping
import com.tajmoti.tulip.mapper.TvShowMappingMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidTvShowMappingRepository @Inject constructor(
    private val dao: ItemMappingDao
) : TvShowMappingRepository {
    private val adapter = TvShowMappingDbAdapter()
    private val mapper = TvShowMappingMapper()

    override suspend fun createTmdbTvMapping(hosted: TvShowKey.Hosted, tmdb: TvShowKey.Tmdb) {
        adapter.insertToDb(dao, ItemMapping(hosted.streamingService, hosted.id, tmdb.id))
    }

    override fun getTmdbMappingForTvShow(tmdb: TvShowKey.Tmdb): Flow<Set<TvShowKey.Hosted>> {
        return adapter.findByKeyFromByTmdbKey(dao, tmdb)
            .map { mappings -> mappings.map { mapping -> mapper.fromDb(mapping) }.toSet() }
    }
}