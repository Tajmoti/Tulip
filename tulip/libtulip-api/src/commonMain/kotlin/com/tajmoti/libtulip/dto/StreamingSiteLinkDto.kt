package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.IdentityItem
import com.tajmoti.libtulip.model.info.LanguageCode

data class StreamingSiteLinkDto(
    val serviceName: String,
    val url: String,
    val linkExtractionSupported: Boolean,
    val language: LanguageCode
) : IdentityItem<Pair<String, String>> {
    override val key = serviceName to url
}