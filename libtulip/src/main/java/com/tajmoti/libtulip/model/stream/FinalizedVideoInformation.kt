package com.tajmoti.libtulip.model.stream

import com.tajmoti.libtulip.model.info.LanguageCode

sealed class FinalizedVideoInformation {
    abstract val serviceName: String
    abstract val language: LanguageCode
    abstract val url: String

    data class Direct(
        override val serviceName: String,
        override val url: String,
        override val language: LanguageCode,
        val dimensions: VideoDimensions?
    ) : FinalizedVideoInformation()

    data class Website(
        override val serviceName: String,
        override val url: String,
        override val language: LanguageCode
    ) : FinalizedVideoInformation()
}