package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.TvItemInfo
import org.jsoup.nodes.Document


fun parseTvItemInfo(key: String, page: Document): TvItemInfo {
    val title =
        page.selectFirst(".stage_navigation > h1:nth-child(1) > span:nth-child(1) > a:nth-child(1)")!!
            .attr("title")
    val yearStart = title.indexOfLast { it == '(' }
    val yearEnd = title.indexOfLast { it == ')' }
    val yearStr = title.subSequence(yearStart + 1, yearEnd)
    val name = title.substring(0, yearStart - 1)
    return TvItemInfo(key, name, "en", yearStr.toString().toInt())
}