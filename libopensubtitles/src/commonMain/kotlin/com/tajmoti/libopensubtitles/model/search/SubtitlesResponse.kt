package com.tajmoti.libopensubtitles.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubtitlesResponse(
    @SerialName("data")
    val data: List<SubtitlesResponseData>
)
