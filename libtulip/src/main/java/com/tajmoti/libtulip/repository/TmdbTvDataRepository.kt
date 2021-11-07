package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.flatMap
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TmdbTvDataRepository {

    fun findTmdbIdAsFlow(searchResult: SearchResult): Flow<NetworkResult<TmdbItemId?>>

    suspend fun findTmdbId(searchResult: SearchResult): TmdbItemId?


    suspend fun searchTv(query: String, firstAirDateYear: Int?): Result<SearchTvResponse>

    suspend fun searchMovie(query: String, firstAirDateYear: Int?): Result<SearchMovieResponse>


    fun getItem(key: ItemKey.Tmdb): Flow<NetworkResult<out TulipItem.Tmdb>> {
        return when (key) {
            is TvShowKey.Tmdb -> getTvShowWithSeasons(key)
            is MovieKey.Tmdb -> getMovie(key)
        }
    }

    fun getTvShowWithSeasons(key: TvShowKey.Tmdb): Flow<NetworkResult<out TulipTvShowInfo.Tmdb>>

    fun getSeason(key: SeasonKey.Tmdb): Flow<NetworkResult<out TulipSeasonInfo.Tmdb>>

    fun getEpisode(key: EpisodeKey.Tmdb): Flow<NetworkResult<out TulipEpisodeInfo.Tmdb>>

    fun getFullEpisodeData(key: EpisodeKey.Tmdb): Flow<Result<TulipCompleteEpisodeInfo.Tmdb>> {
        return getTvShowWithSeasons(key.tvShowKey)
            .map {
                it.toResult().flatMap { tvInfo ->
                    val seasons = tvInfo.seasons
                        .firstOrNull { season -> key.seasonNumber == season.seasonNumber }
                        ?: return@map Result.failure(MissingEntityException)
                    val episode = seasons.episodes
                        .firstOrNull { episode -> episode.episodeNumber == key.episodeNumber }
                        ?: return@map Result.failure(MissingEntityException)
                    Result.success(TulipCompleteEpisodeInfo.Tmdb(key, tvInfo.name, episode))
                }
            }
    }

    fun getStreamableInfo(key: StreamableKey.Tmdb): Flow<Result<StreamableInfo.Tmdb>> {
        return when (key) {
            is EpisodeKey.Tmdb -> getFullEpisodeData(key)
            is MovieKey.Tmdb -> getMovie(key).map { it.toResult() }
        }
    }

    fun getMovie(key: MovieKey.Tmdb): Flow<NetworkResult<out TulipMovie.Tmdb>>
}