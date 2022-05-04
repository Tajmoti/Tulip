package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.SeasonDto
import com.tajmoti.libtulip.dto.TvShowDto
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.result.NetworkResult
import kotlinx.coroutines.flow.Flow

interface TvShowInfoFacade {

    fun getTvShowInfo(key: TvShowKey): Flow<NetworkResult<TvShowDto>>

    fun getSeason(key: SeasonKey): Flow<NetworkResult<SeasonDto>>

    fun getStreamableInfo(key: StreamableKey): Flow<Result<StreamableInfo>>
}