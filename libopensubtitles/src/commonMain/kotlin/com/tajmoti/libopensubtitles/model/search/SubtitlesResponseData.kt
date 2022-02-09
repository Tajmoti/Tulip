package com.tajmoti.libopensubtitles.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubtitlesResponseData(
    @SerialName("attributes")
    val attributes: SubtitleAttributes
)
