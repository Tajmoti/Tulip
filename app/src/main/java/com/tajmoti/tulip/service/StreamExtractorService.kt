package com.tajmoti.tulip.service

import com.tajmoti.tulip.model.StreamableInfo
import com.tajmoti.tulip.model.StreamableInfoWithLinks

interface StreamExtractorService {

    suspend fun fetchStreams(streamable: StreamableInfo): Result<StreamableInfoWithLinks>
}