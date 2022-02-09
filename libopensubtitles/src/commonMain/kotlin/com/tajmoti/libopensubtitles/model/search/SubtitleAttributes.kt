package com.tajmoti.libopensubtitles.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubtitleAttributes(
    @SerialName("release")
    val release: String,
    @SerialName("language")
    val language: String,
    @SerialName("subtitle_id")
    val subtitleId: Long,
    @SerialName("legacy_subtitle_id")
    val legacySubtitleId: Long,
    @SerialName("files")
    val data: List<SubtitleFile>
)