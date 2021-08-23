package com.tajmoti.libtulip.model.info

sealed class StreamableInfo {

    data class Episode(
        val showName: String,
        val seasonNumber: Int,
        val info: TulipEpisodeInfo
    ) : StreamableInfo()

    data class Movie(
        val name: String
    ) : StreamableInfo()
}