package com.tajmoti.tulip.service.impl

import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import com.tajmoti.tulip.model.StreamableInfo
import com.tajmoti.tulip.model.StreamableInfoWithLinks
import com.tajmoti.tulip.service.StreamExtractorService
import com.tajmoti.tulip.ui.streams.UnloadedVideoStreamRef
import javax.inject.Inject

class StreamsExtractionServiceImpl @Inject constructor(
    private val linkExtractor: VideoLinkExtractor
) : StreamExtractorService {

    override suspend fun fetchStreams(streamable: StreamableInfo): Result<StreamableInfoWithLinks> {
        val result = streamable.streamable.loadSources().getOrElse {
            return Result.failure(it)
        }
        val sorted = mapAndSortLinksByRelevance(result)
        return Result.success(StreamableInfoWithLinks(streamable, sorted))
    }

    private fun mapAndSortLinksByRelevance(it: List<VideoStreamRef>): List<UnloadedVideoStreamRef> {
        val mapped = it.map { UnloadedVideoStreamRef(it, linkExtractor.canExtractLink(it.url)) }
        val working = mapped.filter { it.info.working }
        val broken = mapped.filterNot { it.info.working }
        val extractable = working.filter { it.linkExtractionSupported }
        val notExtractable = working.filterNot { it.linkExtractionSupported }
        val badExtractable = broken.filter { it.linkExtractionSupported }
        val badNotExtractor = broken.filterNot { it.linkExtractionSupported }
        return extractable + notExtractable + badExtractable + badNotExtractor
    }
}