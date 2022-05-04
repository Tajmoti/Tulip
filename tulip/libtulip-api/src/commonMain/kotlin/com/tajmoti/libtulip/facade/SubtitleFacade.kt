package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.SubtitleDto
import com.tajmoti.libtulip.model.key.StreamableKey

interface SubtitleFacade {

    /**
     * Retrieves the list of available subtitles for the TV show episode or movie specified by [key].
     */
    suspend fun getAvailableSubtitles(key: StreamableKey): Result<List<SubtitleDto>>
}