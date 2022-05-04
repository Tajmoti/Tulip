package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.SubtitleKey

data class SubtitleDto(
    val key: SubtitleKey,
    val release: String,
    val language: String,
    val fileId: Long,
)
