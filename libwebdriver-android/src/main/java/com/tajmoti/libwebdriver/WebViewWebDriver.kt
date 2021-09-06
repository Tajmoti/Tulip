package com.tajmoti.libwebdriver

import android.annotation.TargetApi
import android.content.Context
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.webkit.*
import com.tajmoti.commonutils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class WebViewWebDriver(
    val context: Context,
    private val mainHandler: Handler = Handler(context.mainLooper),
    private val blockImages: Boolean = false,
    private val suppressConsole: Boolean = true
) : WebDriver {
    private val chromeClient: ChromeClient by lazy(LazyThreadSafetyMode.NONE) {
        ChromeClient(suppressConsole)
    }

    override suspend fun getPageHtml(url: String, params: WebDriver.Params): Result<String> {
        return withContext(Dispatchers.Main) { loadPageOnMainThread(url, params) }
    }

    private suspend fun loadPageOnMainThread(url: String, p: WebDriver.Params): Result<String> {
        return suspendCoroutine { continuation ->
            loadPageWithContinuation(url, p, continuation)
        }
    }

    private fun loadPageWithContinuation(
        url: String,
        p: WebDriver.Params,
        cont: Continuation<Result<String>>
    ) {
        var finished = false
        fun submitOnce(wv: WebView, result: Result<String>) {
            if (finished)
                return
            cont.resumeWith(Result.success(result))
            wv.destroy()
            finished = true
        }
        val wv = createWebView(p,
            { wv, html ->
                mainHandler.post { submitOnce(wv, Result.success(html)) }
            },
            { wv, error ->
                logger.warn("WebView returned error {}", error)
                val throwable = Exception("Received error '$error'")
                mainHandler.post { submitOnce(wv, Result.failure(throwable)) }
            })
        mainHandler.postDelayed({
            submitOnce(wv, Result.failure(TimeoutException()))
        }, p.timeoutMs)
        wv.loadUrl(url)
    }

    private fun createWebView(
        p: WebDriver.Params,
        onSuccess: (WebView, String) -> Unit,
        onError: (WebView, Any) -> Unit
    ): WebView {
        @Suppress("SetJavaScriptEnabled")
        return WebView(context)
            .apply { settings.javaScriptEnabled = true }
            .apply { settings.blockNetworkImage = blockImages }
            .apply { addJavascriptInterface(JSInterface { onSuccess(this, it) }, CLIENT_NAME) }
            .apply { webViewClient = HtmlRetrievingClient(p.urlFilter, onError) }
            .apply { webChromeClient = chromeClient }
    }

    private class HtmlRetrievingClient(
        private val filter: UrlFilter?,
        private val onError: (WebView, Any) -> Unit
    ) : WebViewClient() {
        companion object {
            private const val HTML_RETRIEVER_JS =
                """
                window
                .$CLIENT_NAME
                .getHtml(document.getElementsByTagName('html')[0].outerHTML);
                """
        }

        override fun onPageFinished(view: WebView, url: String) {
            view.loadUrl("javascript:$HTML_RETRIEVER_JS")
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            if (filter?.invoke(request.url.toString()) != false) {
                return null
            }
            return WebResourceResponse("", "", null)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(vw: WebView, r: WebResourceRequest, err: WebResourceError) {
            if (r.isForMainFrame)
                onError(
                    vw,
                    "onReceivedError url='${r.url}', errorCode='${err.errorCode}', description='${err.description}'"
                )
        }

        override fun onReceivedHttpError(
            view: WebView,
            r: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            if (r.isForMainFrame)
                onError(
                    view,
                    "onReceivedHttpError url='${r.url}', statusCode='${errorResponse.statusCode}', reasonPhrase='${errorResponse.reasonPhrase}'"
                )
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView,
            handler: HttpAuthHandler,
            host: String,
            realm: String
        ) {
            handler.cancel()
            onError(view, "onReceivedHttpAuthRequest host='$host', realm='$realm'")
        }

        @TargetApi(27)
        override fun onSafeBrowsingHit(
            view: WebView,
            request: WebResourceRequest,
            threatType: Int,
            callback: SafeBrowsingResponse
        ) {
            callback.backToSafety(false)
            onError(view, "onSafeBrowsingHit request='$request', threatType=$threatType")
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError
        ) {
            handler.cancel()
            onError(view, "onReceivedSslError error='$error'")
        }
    }

    private class ChromeClient(val suppressConsole: Boolean) : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            if (suppressConsole)
                return true
            return super.onConsoleMessage(consoleMessage)
        }
    }

    private class JSInterface(private val callback: (String) -> Unit) {
        @Suppress("unused")
        @JavascriptInterface
        fun getHtml(html: String) {
            callback(html)
        }
    }

    companion object {
        private const val CLIENT_NAME = "HtmlGetter"
    }
}
