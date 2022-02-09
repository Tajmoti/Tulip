package com.tajmoti.libopensubtitles

import com.tajmoti.rektor.Rektor
import com.tajmoti.rektor.Template
import com.tajmoti.rektor.params
import io.ktor.client.statement.*
import io.ktor.utils.io.*

class RektorOpenSubtitlesFallbackService(private val rektor: Rektor) : OpenSubtitlesFallbackService {
    private val downloadSubtitlesFallback = Template.get<HttpResponse>("en/subtitleserve/sub/{file_id}")

    override suspend fun downloadSubtitlesFallback(fileId: Long): ByteReadChannel {
        val response = rektor.execute(
            downloadSubtitlesFallback,
            placeholders = params("file_id" to fileId)
        )
        return response.bodyAsChannel()
    }
}