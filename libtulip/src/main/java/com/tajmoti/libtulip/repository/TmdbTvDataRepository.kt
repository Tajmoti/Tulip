package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.mapToAsyncJobsTriple
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.misc.finalValueOrNull
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.misc.takeIfNoneNull
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.TvItemInfo
import kotlinx.coroutines.flow.Flow

interface TmdbTvDataRepository {

    suspend fun prefetchTvShowData(key: TvShowKey.Tmdb): Result<Unit>

    suspend fun findTmdbId(type: SearchResult.Type, info: TvItemInfo): TmdbItemId?


    suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse>

    suspend fun searchMovie(query: String, firstAirDateYear: Int?): Result<SearchMovieResponse>


    fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<Tv>>

    suspend fun getTv(key: TvShowKey.Tmdb): Tv? {
        return getTvAsFlow(key).finalValueOrNull()
    }

    fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<TmdbCompleteTvShow>>


    fun getSeasonAsFlow(key: SeasonKey.Tmdb): Flow<NetworkResult<Season>>


    suspend fun getSeason(key: SeasonKey.Tmdb): Season? {
        return getSeasonAsFlow(key).finalValueOrNull()
    }


    suspend fun getEpisodeAsFlow(key: EpisodeKey.Tmdb): Flow<NetworkResult<Episode>>

    suspend fun getEpisode(key: EpisodeKey.Tmdb): Episode? {
        return getEpisodeAsFlow(key).finalValueOrNull()
    }


    suspend fun getMovie(movieId: TmdbItemId.Movie): Movie?


    suspend fun getFullEpisodeData(key: EpisodeKey.Tmdb): Triple<Tv, Season, Episode>? {
        return mapToAsyncJobsTriple(
            { getTv(key.seasonKey.tvShowKey) },
            { getSeason(key.seasonKey) },
            { getEpisode(key) }
        ).takeIfNoneNull()
    }
}