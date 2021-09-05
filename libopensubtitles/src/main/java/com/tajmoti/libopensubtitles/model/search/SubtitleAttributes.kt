package com.tajmoti.libopensubtitles.model.search

import com.squareup.moshi.Json

data class SubtitleAttributes(
    @field:Json(name = "release")
    val release: String,
    @field:Json(name = "language")
    val language: String,
    @field:Json(name = "subtitle_id")
    val subtitleId: Long,
    @field:Json(name = "legacy_subtitle_id")
    val legacySubtitleId: Long,
    @field:Json(name = "files")
    val data: List<SubtitleFile>
)