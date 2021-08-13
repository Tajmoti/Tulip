package com.tajmoti.tulip.service

import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.model.*
import com.tajmoti.tulip.model.key.SeasonKey
import com.tajmoti.tulip.model.key.StreamableKey
import com.tajmoti.tulip.model.key.TvShowKey

interface TvDataService {

    suspend fun getTvShow(key: TvShowKey): Result<TvItem.Show>

    suspend fun getSeason(key: SeasonKey): Result<Season>

    suspend fun getStreamable(key: StreamableKey): Result<StreamableInfo>

    suspend fun searchAndSaveItems(query: String): Result<List<Pair<StreamingService, TvItem>>>

    suspend fun fetchAndSaveSeasons(key: TvShowKey, show: TvItem.Show): Result<List<Season>>
}