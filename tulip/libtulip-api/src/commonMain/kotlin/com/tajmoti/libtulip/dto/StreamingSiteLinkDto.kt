package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.IdentityItem

data class StreamingSiteLinkDto(
    val serviceName: String,
    val url: String,
    val linkExtractionSupported: Boolean,
    val language: LanguageCodeDto
) : IdentityItem<Pair<String, String>> {
    override val key = serviceName to url
}