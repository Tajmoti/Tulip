package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.search.TulipSearchResult
import com.tajmoti.libtvprovider.VideoStreamRef
import kotlinx.coroutines.flow.Flow

/**
 * Handles data coming from specific streaming sites.
 */
interface HostedTvDataRepository {

    fun search(query: String): Flow<Result<List<TulipSearchResult>>>

    fun getTvShow(key: TvShowKey.Hosted): Flow<NetworkResult<TulipTvShowInfo.Hosted>>

    fun getTvShowsByTmdbKey(key: TvShowKey.Tmdb): Flow<List<Result<TulipTvShowInfo.Hosted>>>

    fun getSeasons(key: TvShowKey.Hosted): Flow<NetworkResult<List<TulipSeasonInfo.Hosted>>>

    fun getSeason(key: SeasonKey.Hosted): Flow<NetworkResult<TulipSeasonInfo.Hosted>>

    fun getStreamableInfo(key: StreamableKey.Hosted): Flow<Result<StreamableInfo.Hosted>>

    fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Flow<List<Result<TulipEpisodeInfo.Hosted>>>

    fun getCompleteEpisodesByTmdbKey(key: EpisodeKey.Tmdb): Flow<List<Result<TulipCompleteEpisodeInfo.Hosted>>>

    fun getMoviesByTmdbKey(key: MovieKey.Tmdb): Flow<List<Result<TulipMovie.Hosted>>>

    fun getStreamableInfoByTmdbKey(key: StreamableKey.Tmdb): Flow<List<Result<StreamableInfo.Hosted>>> {
        return when (key) {
            is MovieKey.Tmdb -> getMoviesByTmdbKey(key)
            is EpisodeKey.Tmdb -> getCompleteEpisodesByTmdbKey(key)
        }
    }

    fun fetchStreams(key: StreamableKey.Hosted): NetFlow<List<VideoStreamRef>>
}