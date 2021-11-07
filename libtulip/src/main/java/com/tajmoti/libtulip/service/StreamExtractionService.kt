package com.tajmoti.libtulip.service

import arrow.core.Either
import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.ExtractionError

interface StreamExtractionService {

    fun canExtractStream(ref: VideoStreamRef): Boolean

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Either<ExtractionError, String>
}