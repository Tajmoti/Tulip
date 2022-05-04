package com.tajmoti.libtulip.ui.search

import com.tajmoti.libtulip.model.hosted.TvItemInfo
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.dto.SearchResultDto
import com.tajmoti.libtulip.ui.languageComparator

object SearchUi {

    fun getItemInfoForDisplay(item: SearchResultDto, lang: LanguageCode = LanguageCode("en")): TvItemInfo {
        val firstResult = item.results.firstOrNull { it.info.language == lang.code }
            ?: item.results.first()
        return firstResult.info
    }

    fun getLanguagesForItem(item: SearchResultDto): List<LanguageCode> {
        return item.results
            .map { it.info.language }
            .distinct()
            .map { LanguageCode(it) }
            .sortedWith(languageComparator)
    }
}