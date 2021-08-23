package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtvprovider.VideoStreamRef

interface StreamExtractorService {

    suspend fun fetchStreams(
        service: StreamingService,
        streamableKey: String
    ): Result<List<UnloadedVideoStreamRef>>

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String>
}