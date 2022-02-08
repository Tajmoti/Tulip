package com.tajmoti.rektor

import mu.KotlinLogging

class LoggingRektor(val impl: Rektor) : Rektor by impl {
    private val logger = KotlinLogging.logger {}

    override suspend fun <T : Any> execute(
        template: Template<T>,
        queryParams: Map<String, String>,
        placeholders: Map<String, String>
    ): T {
        val url = template.url
        val requestLogLine = ">>> ${template.method} $url $queryParams"
        try {
            val response = impl.execute(template, queryParams, placeholders)
            logger.debug { "$requestLogLine\n<<< $response" }
            return response
        } catch (e: Throwable) {
            logger.debug(e) { "$requestLogLine\n<<< Exception!" }
            throw e
        }
    }
}