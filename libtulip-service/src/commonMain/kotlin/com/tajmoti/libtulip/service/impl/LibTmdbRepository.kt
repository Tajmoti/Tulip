package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.SlimSeason
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class LibTmdbRepository(private val service: TmdbService) : TmdbTvDataRepository {

    override fun findTvShowKey(name: String, firstAirYear: Int?): Flow<NetworkResult<TvShowKey.Tmdb?>> {
        logger.debug { "Looking up TMDB ID for TV show $name ($firstAirYear)" }
        return flow { emit(fetchSearchResultTv(TitleQuery(name, firstAirYear)).toLibResult()) }
    }

    private suspend fun fetchSearchResultTv(query: TitleQuery): Result<TvShowKey.Tmdb?> {
        return searchTv(query)
            .map { firstResultIdOrNull(it) }
            .map { it?.let { TvShowKey.Tmdb(it) } }
            .onFailure { logger.warn(it) { "Exception searching $query" } }
    }


    override fun findMovieKey(name: String, firstAirYear: Int?): Flow<NetworkResult<MovieKey.Tmdb?>> {
        logger.debug { "Looking up TMDB ID for movie $name ($firstAirYear)" }
        return flow { emit(fetchSearchResultMovie(TitleQuery(name, firstAirYear)).toLibResult()) }
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


    override fun getTvShow(key: TvShowKey.Tmdb): Flow<NetworkResult<TvShow.Tmdb>> {
        return flow { emit(getTvShowLib(key)) }
    }

    private suspend fun getTvShowLib(key: TvShowKey.Tmdb): NetworkResult<TvShow.Tmdb> {
        return runCatching { service.getTv(key.id).fromNetwork() }
            .toLibResult()
    }


    override fun getSeasonWithEpisodes(key: SeasonKey.Tmdb): NetFlow<SeasonWithEpisodes.Tmdb> {
        return flow { emit(getSeasonLib(key)) }
    }

    private suspend fun getSeasonLib(key: SeasonKey.Tmdb): NetworkResult<SeasonWithEpisodes.Tmdb> {
        return runCatching { service.getSeason(key.tvShowKey.id, key.seasonNumber).fromNetwork(key.tvShowKey) }
            .toLibResult()
    }

    override fun getMovie(key: MovieKey.Tmdb): Flow<NetworkResult<TulipMovie.Tmdb>> {
        return fetchMovieInfo(key).map { it.toLibResult() }
    }

    private fun fetchMovieInfo(key: MovieKey.Tmdb): Flow<Result<TulipMovie.Tmdb>> {
        logger.debug { "Downloading movie info of $key" }
        return flow { emit(runCatching { service.getMovie(key.id).fromNetwork() }) }
    }


    private fun <T> Result<T>.toLibResult(): NetworkResult<T> {
        return fold(
            { NetworkResult.Success(it) },
            { NetworkResult.Error(NetworkResult.ErrorType.CONVERSION_FAILED) }
        )
    }

    private fun Movie.fromNetwork(): TulipMovie.Tmdb {
        val key = MovieKey.Tmdb(id)
        return TulipMovie.Tmdb(key, name, overview, posterPath, backdropPath)
    }

    private fun Tv.fromNetwork(): TvShow.Tmdb {
        val key = TvShowKey.Tmdb(id)
        val baseImageUrl = "https://image.tmdb.org/t/p/original"
        val seasons = seasons.map { it.fromNetwork(key) }
        return TvShow.Tmdb(key, name, null, baseImageUrl + posterPath, baseImageUrl + backdropPath, seasons)
    }

    private fun SlimSeason.fromNetwork(tvShowKey: TvShowKey.Tmdb): com.tajmoti.libtulip.model.info.Season.Tmdb {
        val key = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        return com.tajmoti.libtulip.model.info.Season.Tmdb(key, name, seasonNumber, overview)
    }

    private fun Season.fromNetwork(tvShowKey: TvShowKey.Tmdb): SeasonWithEpisodes.Tmdb {
        val key = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        val season = com.tajmoti.libtulip.model.info.Season.Tmdb(key, name, seasonNumber, overview)
        val episodes = episodes.map { it.fromNetwork(key) }
        return SeasonWithEpisodes.Tmdb(season, episodes)
    }

    private fun com.tajmoti.libtmdb.model.tv.Episode.fromNetwork(seasonKey: SeasonKey.Tmdb): Episode.Tmdb {
        val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
        return Episode.Tmdb(
            key,
            name,
            overview,
            "https://image.tmdb.org/t/p/original$stillPath",
            voteAverage.takeUnless { it == 0.0f })
    }

    data class TitleQuery(
        val name: String,
        val firstAirDate: Int?
    )
}