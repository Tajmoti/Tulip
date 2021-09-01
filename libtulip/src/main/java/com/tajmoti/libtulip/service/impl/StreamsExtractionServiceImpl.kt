package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.stream.FinalizedVideoInformation
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.model.stream.UnloadedVideoWithLanguage
import com.tajmoti.libtulip.model.stream.VideoDimensions
import com.tajmoti.libtulip.service.StreamExtractorService
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.VideoLinkExtractor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.mp4parser.BasicContainer
import org.mp4parser.Box
import org.mp4parser.PropertyBoxParserImpl
import org.mp4parser.boxes.iso14496.part12.TrackBox
import org.mp4parser.boxes.iso14496.part12.VideoMediaHeaderBox
import org.mp4parser.tools.Path
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.nio.channels.Channels
import javax.inject.Inject

class StreamsExtractionServiceImpl @Inject constructor(
    private val linkExtractor: VideoLinkExtractor,
    private val httpClient: HttpClient,
    private val tvProvider: MultiTvProvider<StreamingService>
) : StreamExtractorService {

    override suspend fun fetchStreams(
        service: StreamingService,
        streamableKey: String
    ): Result<List<UnloadedVideoStreamRef>> {
        val result = tvProvider.getStreamableLinks(service, streamableKey).getOrElse {
            return Result.failure(it)
        }
        val sorted = result.map { UnloadedVideoStreamRef(it, canExtractFromService(it)) }
        return Result.success(sorted)
    }

    private fun canExtractFromService(ref: VideoStreamRef): Boolean {
        return linkExtractor.canExtractUrl(ref.url)
                || linkExtractor.canExtractService(ref.serviceName)
    }

    override suspend fun resolveStream(ref: VideoStreamRef.Unresolved): Result<VideoStreamRef.Resolved> {
        val realUrl = resolveRedirects(ref.url)
            .getOrElse { return Result.failure(it) }
        // If there were no redirects, assume that the link already points to the streaming page
        return Result.success(ref.asResolved(realUrl ?: ref.url))
    }

    /**
     * Performs a GET request to the [url] and returns the value of the `location` response header.
     * If the header is missing, returns null.
     */
    private suspend fun resolveRedirects(url: String): Result<String?> {
        return runCatching {
            val response: HttpResponse = httpClient.request(url)
            response.headers["location"]
        }
    }

    override suspend fun extractVideoLink(info: VideoStreamRef.Resolved): Result<String> {
        return linkExtractor.extractVideoLink(info.url, info.serviceName)
            .onFailure { logger.warn("Link extraction for $info failed!", it) }
    }

    override suspend fun getVideoDimensions(videoUrl: String): VideoDimensions? {
        return withContext(Dispatchers.IO) {
            getVideoDimensionsFromUrl(videoUrl)
        }
    }


    private fun getVideoDimensionsFromUrl(link: String): VideoDimensions? {
        return try {
            Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36")
                .method(Connection.Method.GET)
                .execute()
                .bodyStream()
                .use { getDimensionsFromInputStream(it) }
        } catch (e: Throwable) {
            logger.warn("Failed to extract direct link", e)
            return null
        }
    }

    private fun getDimensionsFromInputStream(it: InputStream): VideoDimensions? {
        val container = parseBoxes(it)
        val tracks = parseTracks(container)
        val videoTrack = tracks.firstOrNull { isVideoTrack(it) } ?: return null
        val trackBox = videoTrack.trackHeaderBox
        return VideoDimensions(trackBox.width.toInt(), trackBox.height.toInt())
    }

    private fun isVideoTrack(it: TrackBox): Boolean {
        return it.mediaBox.mediaInformationBox.mediaHeaderBox is VideoMediaHeaderBox
    }

    private fun parseTracks(container: BasicContainer): List<TrackBox> {
        return Path.getPaths(container, "moov[0]/trak")
    }

    private fun parseBoxes(input: InputStream): BasicContainer {
        val readableByteChannel = Channels.newChannel(input)
        val container = BasicContainer()
        val boxParser = PropertyBoxParserImpl()
        var current: Box? = null
        while (current == null || "moov" != current.type) {
            current = boxParser.parseBox(readableByteChannel, null)
            container.addBox(current)
        }
        return container
    }

    override suspend fun finalizeVideoInformation(
        video: UnloadedVideoWithLanguage
    ): FinalizedVideoInformation? {
        val resolvedStream = when (val videoInfo = video.video.info) {
            is VideoStreamRef.Unresolved -> resolveStream(videoInfo)
                .getOrNull()
            is VideoStreamRef.Resolved -> videoInfo
        } ?: return null
        val serviceName = video.video.info.serviceName
        val language = video.language
        if (!video.video.linkExtractionSupported)
            return FinalizedVideoInformation.Website(serviceName, video.video.info.url, language)
        val link = extractVideoLink(resolvedStream)
            .getOrElse { return null }
        val dimensions = getVideoDimensions(link)
        return FinalizedVideoInformation.Direct(serviceName, link, language, dimensions)
    }
}