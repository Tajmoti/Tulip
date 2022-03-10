package com.tajmoti.libtmdb

import com.tajmoti.commonutils.allOrNone
import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.logger
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.SlimSeason
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

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


    override fun getTvShow(key: TvShowKey.Tmdb): Flow<NetworkResult<TulipTvShowInfo.Tmdb>> {
        return getTvAsFlow(key)
            .flatMapLatest { it.fold({ tv -> pairTvWithSeasons(tv, key) }, { th -> flowOf(Result.failure(th)) }) }
            .map { it.toLibResult() }
    }

    private fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<Result<Tv>> {
        return flow { emit(runCatching { service.getTv(key.id) }) }
    }

    private fun pairTvWithSeasons(tv: Tv, key: TvShowKey.Tmdb): Flow<Result<TulipTvShowInfo.Tmdb>> {
        return tv.seasons
            .map { season -> getSeasonAsFlow(tv, season, key) }
            .combineNonEmpty()
            .map { it.allOrNone().map { tv.fromNetwork(it) } }
    }

    private fun getSeasonAsFlow(tv: Tv, slim: SlimSeason, key: TvShowKey.Tmdb): Flow<Result<TulipSeasonInfo.Tmdb>> {
        return flow { emit(runCatching { service.getSeason(tv.id, slim.seasonNumber).fromNetwork(key) }) }
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

    private fun Tv.fromNetwork(seasons: List<TulipSeasonInfo.Tmdb>): TulipTvShowInfo.Tmdb {
        val key = TvShowKey.Tmdb(id)
        val baseImageUrl = "https://image.tmdb.org/t/p/original"
        return TulipTvShowInfo.Tmdb(key, name, null, baseImageUrl + posterPath, baseImageUrl + backdropPath, seasons)
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
            "https://image.tmdb.org/t/p/original$stillPath",
            voteAverage.takeUnless { it == 0.0f })
    }

    data class TitleQuery(
        val name: String,
        val firstAirDate: Int?
    )
}