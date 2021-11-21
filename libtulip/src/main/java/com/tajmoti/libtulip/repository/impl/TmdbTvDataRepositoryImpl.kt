package com.tajmoti.libtulip.repository.impl

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
import com.tajmoti.libtulip.misc.cache.TimedCache
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.misc.job.getNetworkBoundResource
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.flow.Flow

class TmdbTvDataRepositoryImpl(
    private val service: TmdbService,
    private val db: LocalTvDataSource,
    config: TulipConfiguration.CacheParameters
) : TmdbTvDataRepository {
    private val tvCache = TimedCache<TvShowKey.Tmdb, TulipTvShowInfo.Tmdb>(
        timeout = config.validityMs, size = config.size
    )
    private val movieCache = TimedCache<MovieKey.Tmdb, TulipMovie.Tmdb>(
        timeout = config.validityMs, size = config.size
    )
    private val tmdbTvIdCache = TimedCache<Pair<String, Int?>, TmdbItemId.Tv?>(
        timeout = config.validityMs, size = config.size
    )
    private val tmdbMovieIdCache = TimedCache<Pair<String, Int?>, TmdbItemId.Movie?>(
        timeout = config.validityMs, size = config.size
    )

    override fun findTmdbIdTv(name: String, firstAirYear: Int?): Flow<NetworkResult<TmdbItemId.Tv?>> {
        logger.debug("Looking up TMDB ID for TV show $name ($firstAirYear)")
        val key = Pair(name, firstAirYear)
        return getNetworkBoundResource(
            { null },
            { fetchSearchResultTv(name, firstAirYear) },
            { },
            { tmdbTvIdCache[key] },
            { tmdbTvIdCache[key] = it }
        )
    }

    override fun findTmdbIdMovie(name: String, firstAirYear: Int?): Flow<NetworkResult<TmdbItemId.Movie?>> {
        logger.debug("Looking up TMDB ID for movie $name ($firstAirYear)")
        val key = Pair(name, firstAirYear)
        return getNetworkBoundResource(
            { null },
            { fetchSearchResultMovie(name, firstAirYear) },
            { },
            { tmdbMovieIdCache[key] },
            { tmdbMovieIdCache[key] = it }
        )
    }

    private suspend fun fetchSearchResultTv(name: String, firstAirDateYear: Int?): Result<TmdbItemId.Tv?> {
        return runCatching {
            searchTv(name, firstAirDateYear)
                .map { firstResultIdOrNull(it) }
                .getOrNull()
                ?.let { TmdbItemId.Tv(it) }
        }.onFailure { logger.warn("Exception searching $name ($firstAirDateYear)") }
    }

    private suspend fun fetchSearchResultMovie(name: String, firstAirDateYear: Int?): Result<TmdbItemId.Movie?> {
        return runCatching {
            searchMovie(name, firstAirDateYear)
                .map { firstResultIdOrNull(it) }
                .getOrNull()
                ?.let { TmdbItemId.Movie(it) }
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
        val tv = runCatching { service.getTv(key.id.id) }
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

    private fun Tv.fromNetwork(seasons: List<TulipSeasonInfo.Tmdb>): TulipTvShowInfo.Tmdb {
        val key = TvShowKey.Tmdb(TmdbItemId.Tv(id))
        return TulipTvShowInfo.Tmdb(key, name, null, posterPath, backdropPath, seasons)
    }

    private fun Season.fromNetwork(tvShowKey: TvShowKey.Tmdb): TulipSeasonInfo.Tmdb {
        val key = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        val episodes = episodes.map { it.fromNetwork(key) }
        return TulipSeasonInfo.Tmdb(key, name, overview, episodes)
    }

    private fun Episode.fromNetwork(seasonKey: SeasonKey.Tmdb): TulipEpisodeInfo.Tmdb {
        val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
        return TulipEpisodeInfo.Tmdb(
            key,
            name,
            overview,
            stillPath,
            voteAverage.takeUnless { it == 0.0f })
    }

    override fun getTvShow(key: TvShowKey.Tmdb): Flow<NetworkResult<TulipTvShowInfo.Tmdb>> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResource(
            { db.getTvShow(key) },
            { fetchFullTvInfo(key) },
            { db.insertTvShow(it) },
            { tvCache[key] },
            { tvCache[key] = it }
        )
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<NetworkResult<TulipMovie.Tmdb>> {
        logger.debug("Retrieving $key")
        return getNetworkBoundResource(
            { db.getMovie(key) },
            { runCatching { service.getMovie(key.id.id).fromNetwork() } },
            { db.insertMovie(it) },
            { movieCache[key] },
            { movieCache[key] = it }
        )
    }

    private fun Movie.fromNetwork(): TulipMovie.Tmdb {
        val key = MovieKey.Tmdb(TmdbItemId.Movie(id))
        return TulipMovie.Tmdb(key, name, overview, posterPath, backdropPath)
    }
}