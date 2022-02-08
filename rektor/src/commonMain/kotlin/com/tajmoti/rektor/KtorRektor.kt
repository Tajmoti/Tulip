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

    override suspend fun <T : Any> execute(
        template: Template<T>,
        queryParams: Map<String, String>,
        placeholders: Map<String, String>,
    ): T {
        val fullUrl = buildUrl(template, placeholders)
        val typeInfo = TypeInfo(template.clazz, template.type.platformType, template.type)
        return doRequest(fullUrl, queryParams).body(typeInfo)
    }

    private suspend fun doRequest(url: String, queryParams: Map<String, String>): HttpResponse {
        return client.get(url) {
            queryParams.forEach { (key, value) -> this.url.parameters[key] = value }
            extraQueryParams.forEach { (key, value) -> this.url.parameters[key] = value }
        }
    }

    private fun <T : Any> buildUrl(template: Template<T>, urlPlaceholders: Map<String, String>): String {
        val finalPath = Templater.buildUrl(template.url, urlPlaceholders)
        return baseUrl + finalPath
    }
}