package com.tajmoti.libopensubtitles.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubtitleFile(
    @SerialName("file_id")
    val fileId: Long,
    @SerialName("file_name")
    val fileName: String
)
