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
import kotlin.coroutines.suspendCoroutine

class WebViewWebDriver(
    /**
     * Context used to instantiate WebView instances and [mainHandler]
     */
    private val context: Context,
    /**
     * Whether to disable all WebView logging messages (suppress log spam)
     */
    private val suppressConsole: Boolean = true
) : TulipWebDriver {
    private val chromeClient: ChromeClient by lazy(LazyThreadSafetyMode.NONE) {
        ChromeClient(suppressConsole)
    }
    private val mainHandler = Handler(context.mainLooper)


    override suspend fun getPageHtml(url: String, params: TulipWebDriver.Params): Result<String> {
        return withContext(Dispatchers.Main) { loadPageWithContinuation(url, params) }
    }

    private suspend fun loadPageWithContinuation(
        url: String,
        p: TulipWebDriver.Params
    ): Result<String> = suspendCoroutine { cont ->
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
        p: TulipWebDriver.Params,
        onSuccess: (WebView, String) -> Unit,
        onError: (WebView, Any) -> Unit
    ): WebView {
        @Suppress("SetJavaScriptEnabled")
        return WebView(context)
            .apply { settings.javaScriptEnabled = true }
            .apply { settings.blockNetworkImage = p.blockImages }
            .apply {
                addJavascriptInterface(
                    JSInterface(this) { onSuccess(this, it) },
                    INTERFACE_NAME
                )
            }
            .apply { webViewClient = HtmlRetrievingClient(p, onError) }
            .apply { webChromeClient = chromeClient }
    }

    private class HtmlRetrievingClient(
        private val p: TulipWebDriver.Params,
        private val onError: (WebView, Any) -> Unit
    ) : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            val js = p.submitTrigger.jsGenerator("window.$INTERFACE_NAME")
            view.evaluateJavascript(js, null)
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            if (p.urlFilter?.invoke(request.url.toString()) != false) {
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

    @Suppress("unused")
    private inner class JSInterface(
        private val wv: WebView,
        private val callback: (String) -> Unit
    ) {
        private val htmlSubmitScript = "window" +
                ".$INTERFACE_NAME" +
                ".submitHtmlInternal(document.documentElement.outerHTML);"

        @JavascriptInterface
        fun submitHtml() {
            mainHandler.post { wv.evaluateJavascript(htmlSubmitScript, null) }
        }

        @JavascriptInterface
        fun submitHtmlInternal(html: String) {
            callback(html)
        }

        @JavascriptInterface
        fun logPrint(message: String) {
            logger.debug(message)
        }
    }

    companion object {
        private const val INTERFACE_NAME = "webDriverInterface"
    }
}
