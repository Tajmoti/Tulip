package com.tajmoti.rektor

class Request<T : Any>(
    val template: Template<T>,
    val queryParams: Map<String, String> = emptyMap(),
    val placeholders: Map<String, String> = emptyMap()
)