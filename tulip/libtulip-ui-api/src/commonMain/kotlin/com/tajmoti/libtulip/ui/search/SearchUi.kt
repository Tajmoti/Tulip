package com.tajmoti.libtulip.ui.search

import com.tajmoti.libtulip.dto.LanguageCodeDto
import com.tajmoti.libtulip.dto.SearchResultDto
import com.tajmoti.libtulip.dto.TvItemInfoDto
import com.tajmoti.libtulip.ui.languageComparator

object SearchUi {

    fun getItemInfoForDisplay(item: SearchResultDto, lang: LanguageCodeDto = LanguageCodeDto("en")): TvItemInfoDto {
        val firstResult = item.results.firstOrNull { it.info.language == lang.code }
            ?: item.results.first()
        return firstResult.info
    }

    fun getLanguagesForItem(item: SearchResultDto): List<LanguageCodeDto> {
        return item.results
            .map { it.info.language }
            .distinct()
            .map { LanguageCodeDto(it) }
            .sortedWith(languageComparator)
    }
}