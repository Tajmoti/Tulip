package com.tajmoti.rektor

data class Request<T : Any>(
    val template: Template<T>,
    val queryParams: Map<String, String> = emptyMap(),
    val placeholders: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val requestBody: Any? = null
)