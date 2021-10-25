package com.tajmoti.libtulip.repository

import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.misc.job.firstValueOrNull
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last

interface TmdbTvDataRepository {

    suspend fun findTmdbIdAsFlow(searchResult: SearchResult): Flow<NetworkResult<TmdbItemId?>>

    suspend fun findTmdbId(searchResult: SearchResult): TmdbItemId?


    suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse>

    suspend fun searchMovie(query: String, firstAirDateYear: Int?): Result<SearchMovieResponse>


    fun getItemAsFlow(key: ItemKey.Tmdb): Flow<NetworkResult<out TulipItem.Tmdb>> {
        return when (key) {
            is TvShowKey.Tmdb -> getTvShowWithSeasonsAsFlow(key)
            is MovieKey.Tmdb -> getMovieAsFlow(key)
        }
    }

    fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<out TulipTvShowInfo.Tmdb>>

    fun getSeasonAsFlow(key: SeasonKey.Tmdb): Flow<NetworkResult<out TulipSeasonInfo.Tmdb>>

    fun getEpisodeAsFlow(key: EpisodeKey.Tmdb): Flow<NetworkResult<out TulipEpisodeInfo.Tmdb>>

    suspend fun getFullEpisodeData(key: EpisodeKey.Tmdb): Triple<TulipTvShowInfo.Tmdb, TulipSeasonInfo.Tmdb, TulipEpisodeInfo.Tmdb>? {
        val tv = getTvShowWithSeasonsAsFlow(key.tvShowKey)
            .last().data ?: return null
        val seasons = tv.seasons
            .firstOrNull { key.seasonNumber == it.seasonNumber } ?: return null
        val episode = seasons.episodes
            .firstOrNull { it.episodeNumber == key.episodeNumber } ?: return null
        return Triple(tv, seasons, episode)
    }


    suspend fun getMovie(movieId: MovieKey.Tmdb): TulipMovie.Tmdb? {
        return getMovieAsFlow(movieId).firstValueOrNull() // TODO
    }

    fun getMovieAsFlow(key: MovieKey.Tmdb): Flow<NetworkResult<out TulipMovie.Tmdb>>
}