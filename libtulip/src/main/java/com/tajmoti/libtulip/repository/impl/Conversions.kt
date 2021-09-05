package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libopensubtitles.model.search.SubtitleAttributes
import com.tajmoti.libopensubtitles.model.search.SubtitlesResponseData
import com.tajmoti.libtulip.model.subtitle.SubtitleInfo

fun SubtitlesResponseData.fromApi(): SubtitleInfo? {
    return attributes.fromApi()
}

fun SubtitleAttributes.fromApi(): SubtitleInfo? {
    val file = data.firstOrNull()?.fileId ?: return null
    return SubtitleInfo(release, language, subtitleId, legacySubtitleId, file)
}
