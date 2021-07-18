package com.tajmoti.tulip.di

import android.content.Context
import android.os.Handler
import com.tajmoti.libwebviewpageloader.WebViewPageLoadParams
import com.tajmoti.libwebviewpageloader.loadPageHtmlViaWebView

class WebViewPageLoader(
    private val context: Context,
    private val mainHandler: Handler,
    private val blockImages: Boolean
) : PageLoader {

    override suspend fun getPageHtml(
        url: String,
        timeoutMs: Long,
        urlFilter: UrlFilter?,
        count: Int
    ): Result<String> {
        val params = WebViewPageLoadParams(
            context,
            mainHandler,
            url,
            timeoutMs,
            blockImages,
            urlFilter,
            count
        )
        return loadPageHtmlViaWebView(params)
    }
}