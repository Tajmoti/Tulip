package com.tajmoti.libtulip.repository

import com.tajmoti.libtvprovider.VideoStreamRef

interface StreamsRepository {

    fun canExtractFromService(ref: VideoStreamRef): Boolean

    suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved>

    suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String>
}