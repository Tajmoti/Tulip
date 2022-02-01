package com.tajmoti.rektor

object TestApi {
    private val FIND = Template<Int>(Method.GET, "/find/")

    fun find(id: String): Request<Int> {
        return Request(FIND, queryParams = mapOf("id" to id))
    }
}