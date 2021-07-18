package com.tajmoti.libwebviewpageloader

import android.annotation.TargetApi
import android.content.Context
import android.net.http.SslError
import android.os.Handler
import android.webkit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

typealias UrlFilter = (url: String) -> Boolean

data class WebViewPageLoadParams(
    val context: Context,
    val mainHandler: Handler = Handler(context.mainLooper),
    val url: String,
    val timeoutMs: Long = 30000L,
    val blockImages: Boolean = false,
    val urlFilter: UrlFilter? = null,
    val count: Int = 1
)

/**
 * Loads the webpage specified by [params] and returns its source,
 * or a failure result in case of a timeout or a loading error.
 */
suspend fun loadPageHtmlViaWebView(params: WebViewPageLoadParams): Result<String> {
    return withContext(Dispatchers.Main) { loadPageOnMainThread(params) }
}

private suspend fun loadPageOnMainThread(params: WebViewPageLoadParams): Result<String> {
    return suspendCoroutine { continuation ->
        loadPageWithContinuation(params, continuation)
    }
}

private sealed class State {
    data class Waiting(val submittedCount: Int) : State()
    object Finished : State()
}

private fun loadPageWithContinuation(p: WebViewPageLoadParams, cont: Continuation<Result<String>>) {
    var state: State = State.Waiting(0)

    fun shouldSubmit(state: State.Waiting, result: Result<String>): Boolean {
        return state.submittedCount == p.count - 1 || result.isFailure
    }

    fun submitOnce(wv: WebView, result: Result<String>) {
        val saved = state
        if (saved !is State.Waiting)
            return
        state = if (shouldSubmit(saved, result)) {
            cont.resumeWith(Result.success(result))
            wv.destroy()
            State.Finished
        } else {
            saved.copy(saved.submittedCount + 1)
        }
    }

    val wv = createWebView(p,
        { wv, html ->
            p.mainHandler.post { submitOnce(wv, Result.success(html)) }
        },
        { wv, error ->
            val throwable = Exception("Received error '$error'")
            p.mainHandler.post { submitOnce(wv, Result.failure(throwable)) }
        })
    p.mainHandler.postDelayed({
        submitOnce(wv, Result.failure(TimeoutException()))
    }, p.timeoutMs)
    wv.loadUrl(p.url)
}

private fun createWebView(
    p: WebViewPageLoadParams,
    onSuccess: (WebView, String) -> Unit,
    onError: (WebView, Any) -> Unit
): WebView {
    @Suppress("SetJavaScriptEnabled")
    return WebView(p.context)
        .apply { settings.javaScriptEnabled = true }
        .apply { settings.blockNetworkImage = p.blockImages }
        .apply { addJavascriptInterface(JSInterface { onSuccess(this, it) }, CLIENT_NAME) }
        .apply { webViewClient = HtmlRetrievingClient(p.urlFilter, onError) }
}

private class HtmlRetrievingClient(
    private val filter: UrlFilter?,
    private val onError: (WebView, Any) -> Unit
) : WebViewClient() {
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

    override fun onReceivedError(vw: WebView, r: WebResourceRequest, err: WebResourceError) {
        if (r.isForMainFrame)
            onError(vw, err)
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        if (request.isForMainFrame)
            onError(view, errorResponse)
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

private class JSInterface(private val callback: (String) -> Unit) {
    @Suppress("unused")
    @JavascriptInterface
    fun getHtml(html: String) {
        callback(html)
    }
}

private const val CLIENT_NAME = "HtmlGetter"
private const val HTML_RETRIEVER_JS =
    """
    window
    .$CLIENT_NAME
    .getHtml(document.getElementsByTagName('html')[0].outerHTML);
    """
