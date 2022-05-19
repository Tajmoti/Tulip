package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.SeasonDto
import com.tajmoti.libtulip.dto.StreamableInfoDto
import com.tajmoti.libtulip.dto.TvShowDto
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.result.NetworkResult
import kotlinx.coroutines.flow.Flow

interface TvShowInfoFacade {

    fun getTvShowInfo(key: TvShowKey): Flow<NetworkResult<TvShowDto>>

    fun getSeason(key: SeasonKey): Flow<NetworkResult<SeasonDto>>

    fun getStreamableInfo(key: StreamableKey): Flow<Result<StreamableInfoDto>>
}