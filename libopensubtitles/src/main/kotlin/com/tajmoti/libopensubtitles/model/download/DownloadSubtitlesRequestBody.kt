package com.tajmoti.libopensubtitles.model.download

import com.squareup.moshi.Json

data class DownloadSubtitlesRequestBody(
    @field:Json(name = "file_id")
    val fileId: Long
)
