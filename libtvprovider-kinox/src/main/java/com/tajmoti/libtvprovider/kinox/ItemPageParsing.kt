package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.TvItemInfo
import org.jsoup.nodes.Document


fun parseTvShowInfo(key: String, document: Document): TvItemInfo {
    val name =
        document.selectFirst("div.leftOpt:nth-child(3) > h1:nth-child(1) > span:nth-child(1)")!!
            .ownText()
    val yearStr = document.selectFirst(".Year")!!
        .ownText()
    val year = yearStr.replace("(", "").replace(")", "")
    val flag = document.select(".Flag > img:nth-child(1)").attr("src")
        .removeSuffix(".png")
        .replaceBeforeLast("/", "")
        .substring(1)
        .toInt()
    return TvItemInfo(key, name, languageNumberToLanguageCode(flag), year.toInt())
}