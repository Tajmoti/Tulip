package com.tajmoti.libtulip.ui

import com.tajmoti.libtulip.PREFERRED_LANGUAGE
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import com.tajmoti.libtulip.model.info.LanguageCode

val videoComparator = Comparator<StreamingSiteLinkDto> { a, b ->
    languageComparator.compare(a.language, b.language)
}

val languageComparator = Comparator<LanguageCode> { a, b ->
    val typeA = getItemType(a)
    val typeB = getItemType(b)
    typeA.compareTo(typeB)
}

private fun getItemType(language: LanguageCode): Int {
    return if (language == PREFERRED_LANGUAGE) -1 else 0
}