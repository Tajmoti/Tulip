package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedMovie
import com.tajmoti.libtulip.model.info.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvShowInfo
import com.tajmoti.libtvprovider.VideoStreamRef

/**
 * Handles data coming from specific streaming sites.
 */
interface HostedTvDataRepository {

    suspend fun search(query: String): Result<List<TulipSearchResult>>

    suspend fun getTvShow(key: TvShowKey.Hosted): Result<TvShowInfo>

    suspend fun getSeasons(key: TvShowKey.Hosted): Result<List<Season>>

    suspend fun getSeason(key: SeasonKey.Hosted): Result<Season>

    suspend fun getStreamableInfo(key: StreamableKey.Hosted): Result<StreamableInfoWithLanguage>

    suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Result<List<HostedEpisode>>

    suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<HostedMovie>>

    suspend fun prefetchTvShow(key: TvShowKey.Hosted): Result<Unit>

    suspend fun prefetchTvShowByTmdbId(key: TvShowKey.Tmdb): Result<Unit>


    suspend fun fetchStreams(key: StreamableKey.Hosted): Result<List<VideoStreamRef>>
}