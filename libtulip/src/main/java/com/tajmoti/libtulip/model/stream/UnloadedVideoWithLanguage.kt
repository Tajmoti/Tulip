package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.LanguageCode

data class UnloadedVideoWithLanguage(
    val video: UnloadedVideoStreamRef,
    /**
     * ISO 639-1 language code of the video's audio
     */
    val language: LanguageCode
)
