package com.tajmoti.rektor

interface Rektor {

    /**
     * Performs an HTTP request described by [template], with optional [queryParams].
     *
     * Any placeholders in the [Template.url] are substituted using the values provided in [placeholders].
     * All the placeholders must be filled, otherwise an [IllegalArgumentException] is thrown.
     */
    suspend fun <T : Any> execute(
        template: Template<T>,
        queryParams: Map<String, String> = emptyMap(),
        placeholders: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestBody: Any? = null
    ): T {
        return execute(Request<T>(template, queryParams, placeholders, headers, requestBody))
    }

    /**
     * Performs an HTTP request described by [request].
     */
    suspend fun <T : Any> execute(request: Request<T>): T
}