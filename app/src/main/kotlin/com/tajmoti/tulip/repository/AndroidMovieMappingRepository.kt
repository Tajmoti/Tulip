package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.MovieMappingRepository
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.adapter.MovieMappingDbAdapter
import com.tajmoti.tulip.db.dao.hosted.TmdbMappingDao
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping
import com.tajmoti.tulip.mapper.AndroidMovieMappingMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidMovieMappingRepository @Inject constructor(
    private val dao: TmdbMappingDao
) : MovieMappingRepository {
    private val adapter = MovieMappingDbAdapter()
    private val mapper = AndroidMovieMappingMapper()

    override suspend fun createTmdbMovieMapping(hosted: MovieKey.Hosted, tmdb: MovieKey.Tmdb) {
        adapter.insertToDb(dao, DbTmdbMapping(hosted.streamingService, hosted.id, tmdb.id))
    }

    override fun getTmdbMappingForMovie(tmdb: MovieKey.Tmdb): Flow<Set<MovieKey.Hosted>> {
        return adapter.findByKeyFromByTmdbKey(dao, tmdb)
            .map { mappings -> mappings.map { mapping -> mapper.fromDb(mapping) }.toSet() }
    }
}