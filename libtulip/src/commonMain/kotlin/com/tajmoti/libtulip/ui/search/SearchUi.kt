package com.tajmoti.libtulip.ui.search

import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtvprovider.model.TvItemInfo

object SearchUi {

    fun getItemInfoForDisplay(item: GroupedSearchResult, lang: LanguageCode = LanguageCode("en")): TvItemInfo {
        val firstResult = item.results.firstOrNull { it.info.language == lang.code }
            ?: item.results.first()
        return firstResult.info
    }

    fun getLanguagesForItem(item: GroupedSearchResult): List<LanguageCode> {
        return item.results
            .map { it.info.language }
            .distinct()
            .sorted()
            .map { LanguageCode(it) }
    }
}