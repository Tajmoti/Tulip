package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.stream.FinalizedVideoInformation
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.model.stream.VideoDimensions
import com.tajmoti.libtvprovider.VideoStreamRef

interface StreamExtractorService {

    suspend fun fetchStreams(
        service: StreamingService,
        streamableKey: String
    ): Result<List<UnloadedVideoStreamRef>>

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String>

    suspend fun getVideoDimensions(videoUrl: String): VideoDimensions?

    suspend fun finalizeVideoInformation(video: UnloadedVideoWithLanguage): FinalizedVideoInformation?
}