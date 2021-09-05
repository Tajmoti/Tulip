package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.logger
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.misc.getNetworkBoundResource
import com.tajmoti.libtulip.model.hosted.toKey
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class TmdbTvDataRepositoryImpl @Inject constructor(
    private val service: TmdbService,
    private val db: LocalTvDataSource
) : TmdbTvDataRepository {

    override suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse> {
        return runCatching { service.searchTv(query, firstAirDateYear) }
    }

    override suspend fun searchMovie(
        query: String,
        firstAirDateYear: Int?
    ): Result<SearchMovieResponse> {
        return runCatching { service.searchMovie(query, firstAirDateYear) }
    }

    override fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<Tv>> {
        val id = key.id.id
        return getNetworkBoundResource(
            { db.getTv(id) },
            { runCatching { service.getTv(id) } },
            { db.insertTv(it) }
        )
    }

    override fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<TmdbCompleteTvShow>> {
        @OptIn(FlowPreview::class)
        return getTvAsFlow(key)
            .flatMapConcat { result -> tvResultToFlowOfCompleteTvShow(result) }
    }

    private fun tvResultToFlowOfCompleteTvShow(
        result: NetworkResult<Tv>
    ): Flow<NetworkResult<TmdbCompleteTvShow>> {
        return when (result) {
            is NetworkResult.Success<Tv> -> tvToCompleteTvShow(result.data)
            is NetworkResult.Error<Tv> -> flowOf(NetworkResult.Error(result.error))
            is NetworkResult.Cached<Tv> -> tvToCompleteTvShow(result.data) // TODO
        }
    }

    private fun tvToCompleteTvShow(tv: Tv): Flow<NetworkResult<TmdbCompleteTvShow>> {
        val seasonFlows = tv.seasons.map { season ->
            getSeasonAsFlow(season.toKey(tv))
        }
        return combine(seasonFlows) { seasonResults ->
            mapNetworkResults(seasonResults, tv)
        }
    }

    private fun mapNetworkResults(
        seasonResults: Array<NetworkResult<Season>>,
        tv: Tv
    ): NetworkResult<TmdbCompleteTvShow> {
        val seasons = seasonResults.map { seasonResult ->
            when (seasonResult) {
                is NetworkResult.Success<Season> -> seasonResult.data
                is NetworkResult.Error<Season> ->
                    return NetworkResult.Error(seasonResult.error)
                is NetworkResult.Cached -> seasonResult.data // TODO
            }
        }
        return NetworkResult.Success(TmdbCompleteTvShow(tv, seasons))
    }

    override fun getSeasonAsFlow(key: SeasonKey.Tmdb): Flow<NetworkResult<Season>> {
        val tvId = key.tvShowKey.id.id
        return getNetworkBoundResource(
            { db.getSeason(tvId, key.seasonNumber) },
            { runCatching { service.getSeason(tvId, key.seasonNumber) } },
            { db.insertSeason(tvId, it) }
        )
    }

    override suspend fun getEpisodeAsFlow(key: EpisodeKey.Tmdb): Flow<NetworkResult<Episode>> {
        val tvId = key.seasonKey.tvShowKey.id.id
        val seasonNumber = key.seasonKey.seasonNumber
        return getNetworkBoundResource(
            { db.getEpisode(tvId, seasonNumber, key.episodeNumber) },
            { runCatching { service.getEpisode(tvId, seasonNumber, key.episodeNumber) } },
            { db.insertEpisode(tvId, it) }
        )
    }

    override suspend fun getMovie(movieId: TmdbItemId.Movie): Movie? {
        return runCatching { service.getMovie(movieId.id) }
            .onFailure { logger.warn("Failed to retrieve movie $movieId", it) }
            .onSuccess { db.insertMovie(it) }
            .getOrElse { db.getMovie(movieId.id) }
    }
}