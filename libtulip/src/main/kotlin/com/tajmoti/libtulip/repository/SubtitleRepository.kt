package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo

interface SubtitleRepository {
    /**
     * Retrieves the list of available subtitles for the TV show episode or movie specified by [key].
     */
    suspend fun getAvailableSubtitles(key: StreamableKey): Result<List<SubtitleInfo>>
}