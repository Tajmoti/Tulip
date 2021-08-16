package com.tajmoti.tulip.service

import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.tulip.model.StreamableInfo
import com.tajmoti.tulip.model.StreamableInfoWithLinks

interface StreamExtractorService {

    suspend fun fetchStreams(streamable: StreamableInfo): Result<StreamableInfoWithLinks>

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String>
}