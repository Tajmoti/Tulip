package com.tajmoti.tulip

fun languageToIcon(language: String): Int? {
    return when (language) {
        "en" -> R.drawable.ic_flag_uk
        "de" -> R.drawable.ic_flag_de
        else -> null
    }
}