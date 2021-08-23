package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.hosted.*
import com.tajmoti.libtulip.model.info.StreamableInfoWithLanguage
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvShowInfo

interface HostedTvDataService {

    suspend fun search(query: String): Result<Map<StreamingService, Result<List<SearchResult>>>>

    suspend fun getTvShow(key: TvShowKey.Hosted): Result<TvShowInfo>

    suspend fun getSeasons(key: TvShowKey.Hosted): Result<List<HostedSeason>>

    suspend fun getSeason(key: SeasonKey.Hosted): Result<Season>

    suspend fun getStreamableInfo(key: StreamableKey.Hosted): Result<StreamableInfoWithLanguage>

    suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): Result<List<HostedEpisode>>

    suspend fun getMovieByTmdbId(key: MovieKey.Tmdb): Result<List<HostedMovie>>

    suspend fun prefetchTvShow(key: TvShowKey.Hosted): Result<Unit>

    suspend fun prefetchTvShowByTmdbId(key: TvShowKey.Tmdb): Result<Unit>

    suspend fun insertHostedItem(item: HostedItem)

    suspend fun insertHostedItems(items: List<HostedItem>)
}