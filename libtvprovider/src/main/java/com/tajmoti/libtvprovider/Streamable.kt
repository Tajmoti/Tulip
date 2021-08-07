package com.tajmoti.libtvprovider

sealed interface Streamable : Marshallable {

    suspend fun loadSources(): Result<List<VideoStreamRef>>
}