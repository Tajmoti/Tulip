package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.repository.MovieMappingRepository
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.adapter.MovieMappingDbAdapter
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.entity.ItemMapping
import com.tajmoti.tulip.mapper.MovieMappingMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidMovieMappingRepository @Inject constructor(
    private val dao: ItemMappingDao
) : MovieMappingRepository {
    private val adapter = MovieMappingDbAdapter()
    private val mapper = MovieMappingMapper()

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        adapter.insertToDb(dao, ItemMapping(hosted.streamingService, hosted.id, tmdb.id))
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return adapter.findByKeyFromByTmdbKey(dao, tmdb)
            .map { mappings -> mappings.map { mapping -> mapper.fromDb(mapping) }.toSet() }
    }
}