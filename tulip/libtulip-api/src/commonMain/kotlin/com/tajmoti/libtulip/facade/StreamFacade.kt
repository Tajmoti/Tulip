package com.tajmoti.libtulip.facade

import arrow.core.Either
import com.tajmoti.libtulip.dto.ExtractionErrorDto
import com.tajmoti.libtulip.dto.StreamListDto
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow

interface StreamFacade {

    fun getStreamsByKey(key: StreamableKey.Hosted): Flow<StreamListDto>

    fun getStreamsByKey(key: StreamableKey.Tmdb): Flow<StreamListDto>

    fun getStreamsByKey(key: StreamableKey): Flow<StreamListDto> {
        return when (key) {
            is StreamableKey.Hosted -> getStreamsByKey(key)
            is StreamableKey.Tmdb -> getStreamsByKey(key)
        }
    }

    suspend fun extractVideoLink(ref: StreamingSiteLinkDto): Either<ExtractionErrorDto, String>
}