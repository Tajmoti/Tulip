package com.tajmoti.libopensubtitles.model.search

import com.squareup.moshi.Json

data class SubtitleFile(
    @field:Json(name = "file_id")
    val fileId: Long,
    @field:Json(name = "file_name")
    val fileName: String
)
