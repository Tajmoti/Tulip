package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import java.io.File
import java.io.InputStream

interface SubtitleRepository {

    suspend fun fetchAvailableSubtitles(itemId: StreamableKey): Result<List<SubtitleInfo>>

    suspend fun downloadSubtitle(info: SubtitleInfo): Result<InputStream>

    suspend fun downloadSubtitleToFile(info: SubtitleInfo, directory: File): Result<File> {
        val subtitleStream = downloadSubtitle(info)
            .onFailure { logger.warn("Failed to download subtitles", it) }
            .getOrElse { return Result.failure(it) }
        val subtitleFile = File(directory, "sub.srt")
        val copyResult = runCatching {
            subtitleFile.outputStream().use { fileOut ->
                subtitleStream.use {
                    it.copyTo(fileOut)
                }
            }
        }
        return copyResult
            .map { Result.success(subtitleFile) }
            .onFailure { logger.warn("Failed to copy subtitles", it) }
            .getOrElse { return Result.failure(it) }
    }
}