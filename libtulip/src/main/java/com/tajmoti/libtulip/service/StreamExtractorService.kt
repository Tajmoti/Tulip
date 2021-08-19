package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.StreamableInfo
import com.tajmoti.libtulip.model.StreamableInfoWithLinks
import com.tajmoti.libtvprovider.VideoStreamRef

interface StreamExtractorService {

    suspend fun fetchStreams(streamable: StreamableInfo): Result<StreamableInfoWithLinks>

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String>
}