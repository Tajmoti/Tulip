package com.tajmoti.libtulip.repository.impl

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.tajmoti.commonutils.*
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.SlimSeason
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.misc.job.createCache
import com.tajmoti.libtulip.misc.job.toFetcherResult
import com.tajmoti.libtulip.misc.job.toNetFlow
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TmdbTvDataRepositoryImpl(
    private val service: TmdbService,
    private val db: LocalTvDataSource,
    config: TulipConfiguration.CacheParameters
) : TmdbTvDataRepository {
    private val tvStore = StoreBuilder
        .from<TvShowKey.Tmdb, TulipTvShowInfo.Tmdb, TulipTvShowInfo.Tmdb>(
            fetcher = Fetcher.ofResultFlow { fetchFullTvInfo(it).mapWithContext(LibraryDispatchers.libraryContext) { result -> result.toFetcherResult() } },
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
            .mapWithContext(LibraryDispatchers.libraryContext) { it.convert { opt -> opt.orNull() } }
    }

    override fun findMovieKey(name: String, firstAirYear: Int?): Flow<NetworkResult<MovieKey.Tmdb?>> {
        logger.debug("Looking up TMDB ID for movie $name ($firstAirYear)")
        return tmdbMovieIdStore.stream(StoreRequest.cached(name to firstAirYear, true))
            .toNetFlow()
            .mapWithContext(LibraryDispatchers.libraryContext) { it.convert { opt -> opt.orNull() } }
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

    private fun fetchFullTvInfo(key: TvShowKey.Tmdb): Flow<Result<TulipTvShowInfo.Tmdb>> {
        logger.debug("Downloading full TV info of $key")
        return getTvAsFlow(key)
            .flatMapLatest { it.fold({ tv -> pairTvWithSeasons(tv, key) }, { th -> flowOf(Result.failure(th)) }) }
    }

    private fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<Result<Tv>> {
        return flow { emit(runCatching { service.getTv(key.id) }) }
    }

    private fun pairTvWithSeasons(tv: Tv, key: TvShowKey.Tmdb): Flow<Result<TulipTvShowInfo.Tmdb>> {
        return tv.seasons
            .map { season -> getSeasonAsFlow(tv, season, key) }
            .combineNonEmpty()
            .mapWithContext(LibraryDispatchers.libraryContext) { it.allOrNone().map(tv::fromNetwork) }
    }

    private fun getSeasonAsFlow(tv: Tv, slim: SlimSeason, key: TvShowKey.Tmdb): Flow<Result<TulipSeasonInfo.Tmdb>> {
        return flow { emit(runCatching { service.getSeason(tv.id, slim.seasonNumber).fromNetwork(key) }) }
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