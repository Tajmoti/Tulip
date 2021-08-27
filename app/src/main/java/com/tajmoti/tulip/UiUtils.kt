package com.tajmoti.tulip

import com.tajmoti.libtulip.model.info.LanguageCode

fun languageToIcon(language: LanguageCode): Int? {
    return when (language.code) {
        "en" -> R.drawable.ic_flag_uk
        "de" -> R.drawable.ic_flag_de
        else -> null
    }
}