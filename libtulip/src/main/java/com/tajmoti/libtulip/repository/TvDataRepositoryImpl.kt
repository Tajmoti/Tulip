package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.logger
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import javax.inject.Inject

class TvDataRepositoryImpl @Inject constructor(
    private val service: TmdbService,
    private val db: WritableTvDataRepository
) : SearchableTvDataRepository {

    override suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse> {
        return runCatching { service.searchTv(query, firstAirDateYear) }
    }

    override suspend fun searchMovie(
        query: String,
        firstAirDateYear: Int?
    ): Result<SearchMovieResponse> {
        return runCatching { service.searchMovie(query, firstAirDateYear) }
    }

    override suspend fun getTv(tvId: Long): Tv? {
        return runCatching { service.getTv(tvId) }
            .onFailure { logger.debug("Exception", it) }
            .onSuccess { db.insertTv(it) }
            .getOrElse { db.getTv(tvId) }
    }

    override suspend fun getSeason(tvId: Long, seasonNumber: Int): Season? {
        return runCatching { service.getSeason(tvId, seasonNumber) }
            .onFailure { logger.debug("Exception", it) }
            .onSuccess { db.insertSeason(tvId, it) }
            .getOrElse { db.getSeason(tvId, seasonNumber) }
    }

    override suspend fun getSeasons(tvId: Long): List<Season> {
        TODO("Not yet implemented")
    }

    override suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Episode? {
        return runCatching { service.getEpisode(tvId, seasonNumber, episodeNumber) }
            .onFailure { logger.debug("Exception", it) }
            .onSuccess { db.insertEpisode(tvId, it) }
            .getOrElse { db.getEpisode(tvId, seasonNumber, episodeNumber) }
    }

    override suspend fun getEpisodes(tvId: Long, seasonNumber: Int): List<Episode> {
        TODO("Not yet implemented")
    }

    override suspend fun getMovie(movieId: Long): Movie? {
        return runCatching { service.getMovie(movieId) }
            .onFailure { logger.debug("Exception", it) }
            .onSuccess { db.insertMovie(it) }
            .getOrElse { db.getMovie(movieId) }
    }
}