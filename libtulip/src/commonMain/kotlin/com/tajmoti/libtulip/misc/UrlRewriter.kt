package com.tajmoti.libtulip.misc

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.util.*

class UrlRewriter private constructor(val wrapper: (String) -> String) {

    class Config(var wrapper: (String) -> String)

    companion object Plugin : HttpClientPlugin<Config, UrlRewriter> {
        override val key: AttributeKey<UrlRewriter> = AttributeKey("UrlRewriter")

        override fun prepare(block: Config.() -> Unit): UrlRewriter = UrlRewriter(Config { it }.apply(block).wrapper)

        override fun install(plugin: UrlRewriter, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.url(plugin.wrapper(context.url.buildString()))
            }
        }
    }
}