package com.tajmoti.libtulip.repository

import com.tajmoti.libtmdb.model.FindResult
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.misc.firstValueOrNull
import com.tajmoti.libtulip.model.hosted.toMovieKey
import com.tajmoti.libtulip.model.hosted.toTvKey
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.TvItemInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last

interface TmdbTvDataRepository {

    suspend fun findTmdbId(type: SearchResult.Type, info: TvItemInfo): TmdbItemId?


    suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse>

    suspend fun searchMovie(query: String, firstAirDateYear: Int?): Result<SearchMovieResponse>


    fun getItemAsFlow(key: TmdbItemId): Flow<NetworkResult<out FindResult>> {
        return when (key) {
            is TmdbItemId.Tv -> getTvAsFlow(key.toTvKey())
            is TmdbItemId.Movie -> getMovieAsFlow(key.toMovieKey())
        }
    }


    fun getTvAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<out Tv>>

    fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<out TmdbCompleteTvShow>>

    fun getSeasonAsFlow(key: SeasonKey.Tmdb): Flow<NetworkResult<out Season>>

    fun getEpisodeAsFlow(key: EpisodeKey.Tmdb): Flow<NetworkResult<out Episode>>

    suspend fun getFullEpisodeData(key: EpisodeKey.Tmdb): Triple<Tv, Season, Episode>? {
        val tv = getTvShowWithSeasonsAsFlow(key.seasonKey.tvShowKey)
            .last().data ?: return null
        val seasons = tv.seasons
            .firstOrNull { key.seasonKey.seasonNumber == it.seasonNumber } ?: return null
        val episode = seasons.episodes
            .firstOrNull { it.episodeNumber == key.episodeNumber } ?: return null
        return Triple(tv.tv, seasons, episode)
    }


    suspend fun getMovie(movieId: TmdbItemId.Movie): Movie? {
        return getMovieAsFlow(movieId.toMovieKey()).firstValueOrNull() // TODO
    }

    fun getMovieAsFlow(key: MovieKey.Tmdb): Flow<NetworkResult<out Movie>>
}