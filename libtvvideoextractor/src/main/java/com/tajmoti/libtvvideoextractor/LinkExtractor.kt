package com.tajmoti.libtvvideoextractor

interface LinkExtractor {

    suspend fun tryExtractLink(url: String): Result<String>

    fun canExtractLink(url: String): Boolean
}