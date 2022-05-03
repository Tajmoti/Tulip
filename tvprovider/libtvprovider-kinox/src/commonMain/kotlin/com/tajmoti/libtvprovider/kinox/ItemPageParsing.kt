package com.tajmoti.libtvprovider.kinox

import com.tajmoti.ksoup.KElement
import com.tajmoti.libtvprovider.model.TvItemInfo

fun parseTvItemInfo(@Suppress("UNUSED_PARAMETER") key: String, document: KElement): TvItemInfo {
    val name = document.selectFirst("div.leftOpt:nth-child(3) > h1:nth-child(1) > span:nth-child(1)")!!
        .ownText()
    val yearStr = document.selectFirst(".Year")!!
        .ownText()
    val year = yearStr.replace("(", "").replace(")", "")
    val flag = document.select(".Flag > img:nth-child(1)").first().attr("src")
        .removeSuffix(".png")
        .replaceBeforeLast("/", "")
        .substring(1)
        .toInt()
    return TvItemInfo(name, languageNumberToLanguageCode(flag), year.toInt())
}