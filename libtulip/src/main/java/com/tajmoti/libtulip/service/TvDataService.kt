package com.tajmoti.libtulip.service

import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbCompleteTvShow
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.SearchResult
import com.tajmoti.libtvprovider.TvItemInfo
import kotlinx.coroutines.flow.Flow

interface TvDataService {

    suspend fun prefetchTvShowData(key: TvShowKey.Tmdb): Result<Unit>

    suspend fun getTvShow(key: TvShowKey.Tmdb): Result<Tv>

    suspend fun getTvShowAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<Tv>>

    suspend fun getTvShowWithSeasonsAsFlow(key: TvShowKey.Tmdb): Flow<NetworkResult<TmdbCompleteTvShow>>

    suspend fun getSeason(key: SeasonKey.Tmdb): Result<Season>

    suspend fun getStreamableInfo(key: StreamableKey.Tmdb): Result<StreamableInfo>

    suspend fun findTmdbId(type: SearchResult.Type, info: TvItemInfo): TmdbItemId?
}