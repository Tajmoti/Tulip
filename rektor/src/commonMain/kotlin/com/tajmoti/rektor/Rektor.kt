package com.tajmoti.rektor

interface Rektor {

    /**
     * Performs an HTTP request described by [template], with optional [queryParams].
     *
     * Any placeholders in the [Template.url] are substituted using the values provided in [urlPlaceholders].
     * All the placeholders must be filled, otherwise an [IllegalArgumentException] is thrown.
     */
    suspend fun <T : Any> execute(
        template: Template<T>,
        queryParams: Map<String, String> = emptyMap(),
        urlPlaceholders: Map<String, String> = emptyMap()
    ): T

    /**
     * Performs an HTTP request described by [request].
     */
    suspend fun <T : Any> execute(request: Request<T>): T {
        return execute(request.template, request.queryParams, request.placeholders)
    }
}