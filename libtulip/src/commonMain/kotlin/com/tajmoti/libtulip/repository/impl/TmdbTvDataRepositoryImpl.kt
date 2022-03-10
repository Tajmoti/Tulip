package com.tajmoti.libtulip.repository.impl

import arrow.core.Option
import com.tajmoti.commonutils.allOrNone
import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.logger
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
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.multiplatform.store.TStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TmdbTvDataRepositoryImpl(
    private val service: TmdbService,
    private val db: LocalTvDataSource,
    config: TulipConfiguration.CacheParameters
) : TmdbTvDataRepository {
    private val tvStore = TStoreFactory.createStore(
        cache = config,
        source = ::fetchFullTvInfo,
        reader = db::getTvShow,
        writer = { _, it -> db.insertTvShow(it) }
    )
    private val movieStore = TStoreFactory.createStore(
        cache = config,
        source = ::fetchMovieInfo,
        reader = db::getMovie,
        writer = { _, it -> db.insertMovie(it) },
    )
    private val tmdbTvIdStore = TStoreFactory.createStore<TitleQuery, Option<TvShowKey.Tmdb>>(
        cache = config,
        source = { flow { emit(getTvResult(it)) } },
    )
    private val tmdbMovieIdStore = TStoreFactory.createStore<TitleQuery, Option<MovieKey.Tmdb>>(
        cache = config,
        source = { flow { emit(getMovieResult(it)) } },
    )


    private suspend fun getTvResult(query: TitleQuery): Result<Option<TvShowKey.Tmdb>> {
        return fetchSearchResultTv(query)
            .map { Option.fromNullable(it) }
    }

    private suspend fun getMovieResult(query: TitleQuery): Result<Option<MovieKey.Tmdb>> {
        return fetchSearchResultMovie(query)
            .map { Option.fromNullable(it) }
    }

    override fun findTvShowKey(name: String, firstAirYear: Int?): Flow<NetworkResult<TvShowKey.Tmdb?>> {
        logger.debug { "Looking up TMDB ID for TV show $name ($firstAirYear)" }
        return tmdbTvIdStore.stream(TitleQuery(name, firstAirYear))
            .map { it.convert { opt -> opt.orNull() } }
    }

    override fun findMovieKey(name: String, firstAirYear: Int?): Flow<NetworkResult<MovieKey.Tmdb?>> {
        logger.debug { "Looking up TMDB ID for movie $name ($firstAirYear)" }
        return tmdbMovieIdStore.stream(TitleQuery(name, firstAirYear))
            .map { it.convert { opt -> opt.orNull() } }
    }

    private suspend fun fetchSearchResultTv(query: TitleQuery): Result<TvShowKey.Tmdb?> {
        return searchTv(query)
            .map { firstResultIdOrNull(it) }
            .map { it?.let { TvShowKey.Tmdb(it) } }
            .onFailure { logger.warn(it) { "Exception searching $query" } }
    }

    private suspend fun fetchSearchResultMovie(query: TitleQuery): Result<MovieKey.Tmdb?> {
        return searchMovie(query)
            .map { firstResultIdOrNull(it) }
            .map { it?.let { MovieKey.Tmdb(it) } }
            .onFailure { logger.warn(it) { "Exception searching $query" } }
    }


    private fun firstResultIdOrNull(r: SearchResponse): Long? {
        return r.results.firstOrNull()?.id
    }


    private suspend fun searchTv(query: TitleQuery): Result<SearchTvResponse> {
        return runCatching { service.searchTv(query.name, query.firstAirDate) }
    }

    private suspend fun searchMovie(query: TitleQuery): Result<SearchMovieResponse> {
        return runCatching { service.searchMovie(query.name, query.firstAirDate) }
    }

    private fun fetchFullTvInfo(key: TvShowKey.Tmdb): Flow<Result<TulipTvShowInfo.Tmdb>> {
        logger.debug { "Downloading full TV info of $key" }
        return getTvAsFlow(key)
            .flatMapLatest { it.fold({ tv -> pairTvWithSeasons(tv, key) }, { th -> flowOf(Result.failure(th)) }) }
    }

    private fun fetchMovieInfo(key: MovieKey.Tmdb): Flow<Result<TulipMovie.Tmdb>> {
        logger.debug { "Downloading movie info of $key" }
        return flow { emit(runCatching { service.getMovie(key.id).fromNetwork() }) }
    }

    private fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<Result<Tv>> {
        return flow { emit(runCatching { service.getTv(key.id) }) }
    }

    private fun pairTvWithSeasons(tv: Tv, key: TvShowKey.Tmdb): Flow<Result<TulipTvShowInfo.Tmdb>> {
        return tv.seasons
            .map { season -> getSeasonAsFlow(tv, season, key) }
            .combineNonEmpty()
            .map { it.allOrNone().map(tv::fromNetwork) }
    }

    private fun getSeasonAsFlow(tv: Tv, slim: SlimSeason, key: TvShowKey.Tmdb): Flow<Result<TulipSeasonInfo.Tmdb>> {
        return flow { emit(runCatching { service.getSeason(tv.id, slim.seasonNumber).fromNetwork(key) }) }
    }

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<NetworkResult<TulipTvShowInfo.Tmdb>> {
        logger.debug { "Retrieving $key" }
        return tvStore.stream(key)
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<NetworkResult<TulipMovie.Tmdb>> {
        logger.debug { "Retrieving $key" }
        return movieStore.stream(key)
    }

    private fun Movie.fromNetwork(): TulipMovie.Tmdb {
        val key = MovieKey.Tmdb(id)
        return TulipMovie.Tmdb(key, name, overview, posterPath, backdropPath)
    }

    data class TitleQuery(
        val name: String,
        val firstAirDate: Int?
    )
}