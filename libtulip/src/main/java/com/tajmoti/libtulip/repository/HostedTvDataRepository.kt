package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.search.TulipSearchResult
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLanguage
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.flow.Flow

/**
 * Handles data coming from specific streaming sites.
 */
interface HostedTvDataRepository {

    suspend fun search(query: String): Result<List<TulipSearchResult>>

    suspend fun getTvShowAsFlow(key: TvShowKey.Hosted): Flow<NetworkResult<out TulipTvShowInfo.Hosted>>

    suspend fun getTvShow(key: TvShowKey.Hosted): Result<TulipTvShowInfo.Hosted>

    suspend fun getSeasonsAsFlow(key: TvShowKey.Hosted): Flow<NetworkResult<List<TulipSeasonInfo.Hosted>>>

    suspend fun getSeasons(key: TvShowKey.Hosted): Result<List<TulipSeasonInfo.Hosted>>

    suspend fun getSeason(key: SeasonKey.Hosted): Result<TulipSeasonInfo.Hosted>

    suspend fun getStreamableInfo(key: StreamableKey.Hosted): Result<StreamableInfoWithLanguage>

    suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Result<List<TulipEpisodeInfo.Hosted>>

    suspend fun getCompleteEpisodesByTmdbId(key: EpisodeKey.Tmdb): Result<List<TulipCompleteEpisodeInfo.Hosted>>

    suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<TulipMovie.Hosted>>


    suspend fun fetchStreams(key: StreamableKey.Hosted): Result<List<VideoStreamRef>>
}