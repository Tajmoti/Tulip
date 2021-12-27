package com.tajmoti.libtulip.repository.impl

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.tajmoti.commonutils.allOrNone
import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.parallelMap
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.misc.job.createCache
import com.tajmoti.libtulip.misc.job.toFetcherResult
import com.tajmoti.libtulip.misc.job.toNetFlow
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TmdbTvDataRepositoryImpl(
    private val service: TmdbService,
    private val db: LocalTvDataSource,
    config: TulipConfiguration.CacheParameters
) : TmdbTvDataRepository {
    private val tvStore = StoreBuilder
        .from<TvShowKey.Tmdb, TulipTvShowInfo.Tmdb, TulipTvShowInfo.Tmdb>(
            fetcher = Fetcher.ofResult { fetchFullTvInfo(it).toFetcherResult() },
            sourceOfTruth = SourceOfTruth.of(
                reader = { db.getTvShow(it) },
                writer = { _, it -> db.insertTvShow(it) },
            )
        )
        .cachePolicy(createCache(config))
        .build()
    private val movieStore = StoreBuilder
        .from<MovieKey.Tmdb, TulipMovie.Tmdb, TulipMovie.Tmdb>(
            fetcher = Fetcher.ofResult { runCatching { service.getMovie(it.id).fromNetwork() }.toFetcherResult() },
            sourceOfTruth = SourceOfTruth.of(
                reader = { db.getMovie(it) },
                writer = { _, it -> db.insertMovie(it) },
            )
        )
        .cachePolicy(createCache(config))
        .build()
    private val tmdbTvIdStore = StoreBuilder
        .from<Pair<String, Int?>, Option<TvShowKey.Tmdb>>(
            fetcher = Fetcher.ofResult { (name, firstAirDate) ->
                fetchSearchResultTv(name, firstAirDate)
                    .map { it?.let { Some(it) } ?: None }
                    .toFetcherResult()
            }
        )
        .cachePolicy(createCache(config))
        .build()
    private val tmdbMovieIdStore = StoreBuilder
        .from<Pair<String, Int?>, Option<MovieKey.Tmdb>>(
            fetcher = Fetcher.ofResult { (name, firstAirDate) ->
                fetchSearchResultMovie(name, firstAirDate)
                    .map { it?.let { Some(it) } ?: None }
                    .toFetcherResult()
            }
        )
        .cachePolicy(createCache(config))
        .build()

    override fun findTvShowKey(name: String, firstAirYear: Int?): Flow<NetworkResult<TvShowKey.Tmdb?>> {
        logger.debug("Looking up TMDB ID for TV show $name ($firstAirYear)")
        return tmdbTvIdStore.stream(StoreRequest.cached(name to firstAirYear, true))
            .toNetFlow()
            .map { it.convert { opt -> opt.orNull() } }
    }

    override fun findMovieKey(name: String, firstAirYear: Int?): Flow<NetworkResult<MovieKey.Tmdb?>> {
        logger.debug("Looking up TMDB ID for movie $name ($firstAirYear)")
        return tmdbMovieIdStore.stream(StoreRequest.cached(name to firstAirYear, true))
            .toNetFlow()
            .map { it.convert { opt -> opt.orNull() } }
    }

    private suspend fun fetchSearchResultTv(name: String, firstAirDateYear: Int?): Result<TvShowKey.Tmdb?> {
        return runCatching {
            searchTv(name, firstAirDateYear)
                .map { firstResultIdOrNull(it) }
                .getOrNull()
                ?.let { TvShowKey.Tmdb(it) }
        }.onFailure { logger.warn("Exception searching $name ($firstAirDateYear)") }
    }

    private suspend fun fetchSearchResultMovie(name: String, firstAirDateYear: Int?): Result<MovieKey.Tmdb?> {
        return runCatching {
            searchMovie(name, firstAirDateYear)
                .map { firstResultIdOrNull(it) }
                .getOrNull()
                ?.let { MovieKey.Tmdb(it) }
        }.onFailure { logger.warn("Exception searching $name ($firstAirDateYear)") }
    }


    private fun firstResultIdOrNull(r: SearchResponse): Long? {
        return r.results.firstOrNull()?.id
    }


    private suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse> {
        return runCatching { service.searchTv(query, firstAirDateYear) }
    }

    private suspend fun searchMovie(query: String, firstAirDateYear: Int?): Result<SearchMovieResponse> {
        return runCatching { service.searchMovie(query, firstAirDateYear) }
    }

    private suspend inline fun fetchFullTvInfo(key: TvShowKey.Tmdb): Result<TulipTvShowInfo.Tmdb> {
        logger.debug("Downloading full TV info of $key")
        val tv = runCatching { service.getTv(key.id) }
            .getOrElse { return Result.failure(it) }
        return tv.seasons
            .parallelMap { slim ->
                runCatching {
                    service.getSeason(tv.id, slim.seasonNumber).fromNetwork(key)
                }
            }
            .allOrNone()
            .map { tv.fromNetwork(it) }
    }

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<NetworkResult<TulipTvShowInfo.Tmdb>> {
        logger.debug("Retrieving $key")
        return tvStore.stream(StoreRequest.cached(key, false)).toNetFlow()
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<NetworkResult<TulipMovie.Tmdb>> {
        logger.debug("Retrieving $key")
        return movieStore.stream(StoreRequest.cached(key, false)).toNetFlow()
    }

    private fun Movie.fromNetwork(): TulipMovie.Tmdb {
        val key = MovieKey.Tmdb(id)
        return TulipMovie.Tmdb(key, name, overview, posterPath, backdropPath)
    }
}