package com.tajmoti.libtulip.ui

import com.tajmoti.libtulip.PREFERRED_LANGUAGE
import com.tajmoti.libtulip.dto.LanguageCodeDto
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto

val videoComparator = Comparator<StreamingSiteLinkDto> { a, b ->
    languageComparator.compare(a.language, b.language)
}

val languageComparator = Comparator<LanguageCodeDto> { a, b ->
    val typeA = getItemType(a)
    val typeB = getItemType(b)
    typeA.compareTo(typeB)
}

private fun getItemType(language: LanguageCodeDto): Int {
    return if (language == PREFERRED_LANGUAGE) -1 else 0
}