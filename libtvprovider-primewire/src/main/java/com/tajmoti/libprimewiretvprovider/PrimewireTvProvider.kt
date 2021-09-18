package com.tajmoti.libprimewiretvprovider

import com.tajmoti.commonutils.flatMapWithContext
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtvprovider.*
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

class PrimewireTvProvider(
    /**
     * Loads web pages into a real browser to run any JS loading obfuscated data.
     */
    private val pageLoader: PageSourceLoader,
    /**
     * Only performs a GET request, does not run any JS.
     */
    private val httpLoader: SimplePageSourceLoader,
    /**
     * Base URL of the primewire domain, in case it changes.
     */
    private val baseUrl: String = "https://www.primewire.li"
) : TvProvider {

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return httpLoader(queryToSearchUrl(query))
            .flatMapWithContext(Dispatchers.Default) {
                parseSearchResultPageBlocking(it)
            }
    }

    private fun queryToSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "utf-8")
        return "$baseUrl?s=$encoded&t=y&m=m&w=q"
    }

    override suspend fun getTvShow(key: String): Result<TvShowInfo> {
        return httpLoader(baseUrl + key)
            .flatMapWithContext(Dispatchers.Default) { source ->
                val document = Jsoup.parse(source)
                parseSearchResultPageBlockingSeason(key, document)
                    .map { seasons -> document to seasons }
            }
            .mapWithContext(Dispatchers.Default) { (document, seasons) ->
                val tvItemInfo = parseTvItemInfo(key, document)
                TvShowInfo(key, tvItemInfo, seasons)
            }
    }

    private fun parseTvItemInfo(key: String, page: Document): TvItemInfo {
        val title =
            page.selectFirst(".stage_navigation > h1:nth-child(1) > span:nth-child(1) > a:nth-child(1)")!!
                .attr("title")
        val yearStart = title.indexOfLast { it == '(' }
        val yearEnd = title.indexOfLast { it == ')' }
        val yearStr = title.subSequence(yearStart + 1, yearEnd)
        val name = title.substring(0, yearStart - 1)
        return TvItemInfo(key, name, "en", yearStr.toString().toInt())
    }

    override suspend fun getMovie(movieKey: String): Result<MovieInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun getStreamableLinks(episodeOrMovieKey: String): Result<List<VideoStreamRef>> {
        return pageLoader.invoke(baseUrl + episodeOrMovieKey, this::shouldAllowUrl, LINK_PAGE_HTML_SUBMIT_TRIGGER)
            .flatMapWithContext(Dispatchers.Default) {
                getVideoStreamsBlocking(it, baseUrl)
            }
    }


    private fun shouldAllowUrl(url: String): Boolean {
        return url.contains(baseUrl) && URL_BLACKLIST.none { url.contains(it) }
    }

    companion object {
        private val URL_BLACKLIST = listOf("/comments/", "/css/", "/spiderman")


        val LINK_PAGE_HTML_SUBMIT_TRIGGER: (String) -> String = {
            """
            function listenForKeysAdded(links, noKeyLinkCount) {
                const config = {
                    attributes: true,
                    attributeFilter: ["key"]
                };
                var alreadyModified = 0;
                const callback = function (mutationsList, observer) {
                    for (var i = 0; i < mutationsList.length; ++i) {
                        var mutation = mutationsList[i];
                        if (mutation.type !== 'attributes')
                            continue;
                        alreadyModified++;
                        if (alreadyModified === noKeyLinkCount) {
                            $it.submitHtml();
                            return;
                        }
                    }
                };
                const observer = new MutationObserver(callback);
                for (var i = 0; i < links.length; ++i) {
                    var node = links[i];
                    observer.observe(node, config);
                }
            }
            
            function countLinksWithoutKey(links) {
                var linksWithoutKey = 0;
                for (var i = 0; i < links.length; ++i) {
                    var link = links[i];
                    if (!link.hasAttribute("key"))
                        linksWithoutKey++;
                }
                return linksWithoutKey;
            }
            
            const links = document.getElementsByClassName('propper-link');
            var noKeyLinkCount = countLinksWithoutKey(links);
            if (noKeyLinkCount === 0) {
                $it.submitHtml();
            } else {
                listenForKeysAdded(links, noKeyLinkCount);
            }
            """
        }
    }
}