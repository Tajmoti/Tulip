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
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.misc.TimedCache
import com.tajmoti.libtulip.misc.getNetworkBoundResource
import com.tajmoti.libtulip.misc.getNetworkBoundResourceVariable
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.TvItemInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TmdbTvDataRepositoryImpl @Inject constructor(
    private val service: TmdbService,
    private val db: LocalTvDataSource
) : TmdbTvDataRepository {
    private val tvCache = TimedCache<TvShowKey.Tmdb, TulipTvShowInfo.Tmdb>(
        timeout = CACHE_EXPIRY_MS, size = CACHE_SIZE
    )
    private val movieCache = TimedCache<MovieKey.Tmdb, TulipMovie.Tmdb>(
        timeout = CACHE_EXPIRY_MS, size = CACHE_SIZE
    )


    override suspend fun findTmdbId(type: SearchResult.Type, info: TvItemInfo): TmdbItemId? {
        return runCatching {
            when (type) {
                SearchResult.Type.TV_SHOW -> searchTv(info.name, info.firstAirDateYear)
                    .map { firstResultIdOrNull(it) }.getOrNull()?.let { TmdbItemId.Tv(it) }
                SearchResult.Type.MOVIE -> searchMovie(info.name, info.firstAirDateYear)
                    .map { firstResultIdOrNull(it) }.getOrNull()?.let { TmdbItemId.Movie(it) }
            }
        }
            .onFailure { logger.warn("Exception searching $type $info") }
            .getOrNull()
    }

    private fun firstResultIdOrNull(r: SearchResponse): Long? {
        return r.results.firstOrNull()?.id
    }


    override suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse> {
        return runCatching { service.searchTv(query, firstAirDateYear) }
    }

    override suspend fun searchMovie(
        query: String,
        firstAirDateYear: Int?
    ): Result<SearchMovieResponse> {
        return runCatching { service.searchMovie(query, firstAirDateYear) }
    }

    private suspend inline fun fetchFullTvInfo(key: TvShowKey.Tmdb): Result<TulipTvShowInfo.Tmdb> {
        logger.debug("Downloading full TV info")
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
        return TulipEpisodeInfo.Tmdb(key, name, overview)
    }

    override fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<out TulipTvShowInfo.Tmdb>> {
        logger.debug("Getting TV as flow")
        return getNetworkBoundResourceVariable(
            { db.getTvShow(key) },
            { fetchFullTvInfo(key) },
            { db.insertTvShow(it) },
            { it },
            { tvCache[key] },
            { tvCache[key] = it }
        )
    }

    override fun getSeasonAsFlow(key: SeasonKey.Tmdb): Flow<NetworkResult<out TulipSeasonInfo.Tmdb>> {
        logger.debug("Getting season as flow")
        return getNetworkBoundResourceVariable(
            { db.getSeason(key) },
            { fetchFullTvInfo(key.tvShowKey) },
            { db.insertTvShow(it) },
            { getCorrectSeason(it, key) },
            { tvCache[key.tvShowKey] },
            { tvCache[key.tvShowKey] = it }
        )
    }

    override fun getEpisodeAsFlow(key: EpisodeKey.Tmdb): Flow<NetworkResult<out TulipEpisodeInfo.Tmdb>> {
        logger.debug("Getting episode as flow")
        return getNetworkBoundResourceVariable(
            { db.getEpisode(key) },
            { fetchFullTvInfo(key.tvShowKey) },
            { db.insertTvShow(it) },
            { getCorrectEpisode(it, key) },
            { tvCache[key.tvShowKey] },
            { tvCache[key.tvShowKey] = it }
        )
    }

    private fun getCorrectSeason(
        all: TulipTvShowInfo.Tmdb,
        key: SeasonKey.Tmdb
    ): TulipSeasonInfo.Tmdb? {
        return all.seasons.firstOrNull { s -> s.key.seasonNumber == key.seasonNumber }
    }

    private fun getCorrectEpisode(
        all: TulipTvShowInfo.Tmdb,
        key: EpisodeKey.Tmdb
    ): TulipEpisodeInfo.Tmdb? {
        return getCorrectSeason(all, key.seasonKey)?.episodes
            ?.firstOrNull { e -> e.key == key }
    }

    override fun getMovieAsFlow(key: MovieKey.Tmdb): Flow<NetworkResult<out TulipMovie.Tmdb>> {
        logger.debug("Getting Movie as flow")
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

    companion object {
        /**
         * How many TV shows will be cached in memory at max.
         */
        private const val CACHE_SIZE = 16

        /**
         * The memory cache is valid for one hour.
         * After that, the data will be invalidated on the next retrieval.
         */
        private const val CACHE_EXPIRY_MS = 60 * 60 * 1000L
    }
}