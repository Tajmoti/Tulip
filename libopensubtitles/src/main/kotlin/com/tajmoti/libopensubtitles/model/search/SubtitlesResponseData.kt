package com.tajmoti.libopensubtitles.model.search

import com.squareup.moshi.Json

data class SubtitlesResponseData(
    @field:Json(name = "attributes")
    val attributes: SubtitleAttributes
)
