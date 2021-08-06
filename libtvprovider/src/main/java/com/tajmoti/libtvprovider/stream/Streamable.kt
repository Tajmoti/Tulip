package com.tajmoti.libtvprovider.stream

import com.tajmoti.libtvprovider.Marshallable

interface Streamable : Marshallable {

    suspend fun loadSources(): Result<List<VideoStreamRef>>
}