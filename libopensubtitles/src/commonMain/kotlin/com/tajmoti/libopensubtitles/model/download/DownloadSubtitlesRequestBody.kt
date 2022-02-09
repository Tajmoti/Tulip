package com.tajmoti.libopensubtitles.model.download

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadSubtitlesRequestBody(
    @SerialName("file_id")
    val fileId: Long
)
