package com.tajmoti.rektor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.reflect.*

class KtorRektor(
    val client: HttpClient,
    private val baseUrl: String,
    private val extraQueryParams: Map<String, String> = emptyMap()
) : Rektor {

    override suspend fun <T : Any> execute(request: Request<T>): T {
        val fullUrl = buildUrl(request)
        val template = request.template
        val typeInfo = TypeInfo(template.clazz, template.type.platformType, template.type)
        return doRequest(fullUrl, request).body(typeInfo)
    }

    private suspend fun doRequest(url: String, request: Request<*>): HttpResponse {
        val (_, queryParams, _, headers, body) = request
        return client.get(url) {
            queryParams.forEach { (key, value) -> this.url.parameters[key] = value }
            extraQueryParams.forEach { (key, value) -> this.url.parameters[key] = value }
            headers.forEach { (key, value) -> this.headers[key] = value }
            body?.let { setBody(body) }
        }
    }

    private fun <T : Any> buildUrl(request: Request<T>): String {
        val finalPath = Templater.buildUrl(request.template.url, request.placeholders)
        return baseUrl + finalPath
    }
}