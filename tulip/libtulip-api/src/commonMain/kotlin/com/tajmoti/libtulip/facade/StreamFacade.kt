package com.tajmoti.libtulip.facade

import arrow.core.Either
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.dto.StreamListDto
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.libtvvideoextractor.ExtractionError
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


    fun canExtractStream(ref: VideoStreamRef): Boolean

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Either<ExtractionError, String>
}