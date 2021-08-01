package com.tajmoti.libtvprovider.stream

import com.tajmoti.libtvprovider.Marshallable
import com.tajmoti.libtvprovider.NamedItem

interface Streamable: NamedItem, Marshallable {

    suspend fun loadSources(): Result<List<VideoStreamRef>>

    data class Info(
        val name: String
    )
}