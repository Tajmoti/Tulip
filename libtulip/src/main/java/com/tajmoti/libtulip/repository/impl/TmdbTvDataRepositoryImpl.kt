package com.tajmoti.libtulip.repository.impl

import com.tajmoti.commonutils.allOrNone
import com.tajmoti.commonutils.logger
import com.tajmoti.commonutils.mapToAsyncJobsPair
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
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
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
    private val tvCache = TimedCache<TvShowKey.Tmdb, TmdbCompleteTvShow>(
        timeout = CACHE_EXPIRY_MS, size = CACHE_SIZE
    )
    private val movieCache = TimedCache<MovieKey.Tmdb, Movie>(
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

    override fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<out Tv>> {
        // TODO Common implementation with getTvShowWithSeasonsAsFlow
        val id = key.id.id
        logger.debug("Getting TV as flow")
        return getNetworkBoundResourceVariable(
            { db.getTv(id) },
            { fetchFullTvInfo(key) },
            { db.insertCompleteTv(it.tv, it.seasons) },
            { it.tv },
            { tvCache[key] },
            { tvCache[key] = it }
        )
    }

    private suspend inline fun fetchFullTvInfo(key: TvShowKey.Tmdb): Result<TmdbCompleteTvShow> {
        logger.debug("Downloading full TV info")
        val tv = runCatching { service.getTv(key.id.id) }
            .getOrElse { return Result.failure(it) }
        return tv.seasons
            .parallelMap { slim -> runCatching { service.getSeason(tv.id, slim.seasonNumber) } }
            .allOrNone()
            .map { TmdbCompleteTvShow(tv, it) }
    }

    override fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<out TmdbCompleteTvShow>> {
        logger.debug("Getting TV as flow")
        return getNetworkBoundResourceVariable(
            { getFullTvFromDb(key) },
            { fetchFullTvInfo(key) },
            { db.insertCompleteTv(it.tv, it.seasons) },
            { it },
            { tvCache[key] },
            { tvCache[key] = it }
        )
    }

    private suspend fun getFullTvFromDb(key: TvShowKey.Tmdb): TmdbCompleteTvShow? {
        val (tv, seasons) = mapToAsyncJobsPair(
            { db.getTv(key.id.id) },
            { db.getSeasons(key.id.id) }
        )
        tv ?: return null
        return TmdbCompleteTvShow(tv, seasons)
    }

    override fun getSeasonAsFlow(key: SeasonKey.Tmdb): Flow<NetworkResult<out Season>> {
        val tvId = key.tvShowKey.id.id
        logger.debug("Getting season as flow")
        return getNetworkBoundResourceVariable(
            { db.getSeason(tvId, key.seasonNumber) },
            { fetchFullTvInfo(key.tvShowKey) },
            { db.insertCompleteTv(it.tv, it.seasons) },
            { getCorrectSeason(it, key) },
            { tvCache[key.tvShowKey] },
            { tvCache[key.tvShowKey] = it }
        )
    }

    override fun getEpisodeAsFlow(key: EpisodeKey.Tmdb): Flow<NetworkResult<out Episode>> {
        val tvId = key.seasonKey.tvShowKey.id.id
        val seasonNumber = key.seasonKey.seasonNumber
        logger.debug("Getting episode as flow")
        return getNetworkBoundResourceVariable(
            { db.getEpisode(tvId, seasonNumber, key.episodeNumber) },
            { fetchFullTvInfo(key.seasonKey.tvShowKey) },
            { db.insertCompleteTv(it.tv, it.seasons) },
            { getCorrectEpisode(it, key) },
            { tvCache[key.seasonKey.tvShowKey] },
            { tvCache[key.seasonKey.tvShowKey] = it }
        )
    }

    private fun getCorrectSeason(all: TmdbCompleteTvShow, key: SeasonKey.Tmdb): Season? {
        return all.seasons.firstOrNull { s -> s.seasonNumber == key.seasonNumber }
    }

    private fun getCorrectEpisode(all: TmdbCompleteTvShow, key: EpisodeKey.Tmdb): Episode? {
        return getCorrectSeason(all, key.seasonKey)?.episodes
            ?.firstOrNull { e -> e.episodeNumber == key.episodeNumber }
    }

    override fun getMovieAsFlow(key: MovieKey.Tmdb): Flow<NetworkResult<out Movie>> {
        val id = key.id.id
        logger.debug("Getting Movie as flow")
        return getNetworkBoundResource(
            { db.getMovie(id) },
            { runCatching { service.getMovie(key.id.id) } },
            { db.insertMovie(it) },
            { movieCache[key] },
            { movieCache[key] = it }
        )
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