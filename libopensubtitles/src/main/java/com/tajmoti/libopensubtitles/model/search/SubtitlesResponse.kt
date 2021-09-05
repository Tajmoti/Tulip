package com.tajmoti.libopensubtitles.model.search

import com.squareup.moshi.Json

data class SubtitlesResponse(
    @field:Json(name = "data")
    val data: List<SubtitlesResponseData>
)
