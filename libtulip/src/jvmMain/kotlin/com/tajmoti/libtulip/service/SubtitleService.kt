package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.subtitle.SubtitleInfo

interface SubtitleService {
    companion object {
        /**
         * Supported extensions of subtitle files.
         */
        val SUPPORTED_SUBTITLE_TYPES = setOf(".srt", ".sub")
    }

    /**
     * Downloads the subtitles specified by [info] into [directory] and returns the resulting file.
     */
    suspend fun downloadSubtitleToFile(info: SubtitleInfo, directory: String): Result<String>
}