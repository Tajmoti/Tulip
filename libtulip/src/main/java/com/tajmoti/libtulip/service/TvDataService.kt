package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.StreamableInfo
import com.tajmoti.libtulip.model.TulipSearchResult
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem

interface TvDataService {

    suspend fun getTvShow(key: TvShowKey): Result<TvItem.Show>

    suspend fun getSeason(key: SeasonKey): Result<Season>

    suspend fun getStreamable(key: StreamableKey): Result<StreamableInfo>

    suspend fun searchAndSaveItems(query: String): Result<List<TulipSearchResult>>

    suspend fun fetchAndSaveSeasons(key: TvShowKey, show: TvItem.Show): Result<List<Season>>
}