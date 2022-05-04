package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.key.SubtitleKey

interface SubtitleService {
    companion object {
        /**
         * Supported extensions of subtitle files.
         */
        val SUPPORTED_SUBTITLE_TYPES = setOf(".srt", ".sub")
    }

    /**
     * Downloads the subtitles specified by [key] into [directory] and returns the resulting file.
     */
    suspend fun downloadSubtitleToFile(key: SubtitleKey, directory: String): Result<String>
}