package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.flatMap
import com.tajmoti.commonutils.logger
import com.tajmoti.libopensubtitles.OpenSubtitlesFallbackService
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.libtulip.service.SubtitleService
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SubtitleServiceImpl(
    private val openSubtitlesFallbackService: OpenSubtitlesFallbackService
) : SubtitleService {

    override suspend fun downloadSubtitleToFile(info: SubtitleInfo, directory: File): Result<File> {
        return openSubtitleStream(info)
            .flatMap { subtitleStream ->
                val subtitleFile = File(directory, "sub.srt")
                writeStreamToFile(subtitleFile, subtitleStream).map { subtitleFile }
            }
            .onFailure { logger.warn("Failed to save subtitles", it) }
    }

    private fun writeStreamToFile(file: File, stream: InputStream): Result<Unit> {
        return runCatching {
            file.outputStream().use { fileOut ->
                stream.use {
                    it.copyTo(fileOut)
                }
            }
        }
    }

    private suspend fun openSubtitleStream(info: SubtitleInfo): Result<InputStream> {
//        val request = DownloadSubtitlesRequestBody(info.fileId)
//        return runCatching { openSubtitlesService.downloadSubtitles(request) }
//            .mapWithContext(LibraryDispatchers.libraryContext) { it.byteStream() }
        logger.debug("Downloading subtitles by $info")
        return runCatching { openSubtitlesFallbackService.downloadSubtitlesFallback(info.legacyId) }
            .map { ZipInputStream(it.byteStream()) }
            .flatMap { getSubtitleFileFromZip(it) }
    }

    private fun getSubtitleFileFromZip(zis: ZipInputStream): Result<InputStream> {
        var current: ZipEntry?
        val entries = mutableListOf<String>()
        while (zis.nextEntry.also { current = it } != null) {
            val name = current!!.name
            if (SubtitleService.SUPPORTED_SUBTITLE_TYPES.any { name.endsWith(it) })
                return Result.success(zis)
            entries.add(name)
        }
        runCatching { zis.close() }
        return Result.failure(NoSuchElementException("Subtitle file not present in the zip! Zip contents: $entries"))
    }
}