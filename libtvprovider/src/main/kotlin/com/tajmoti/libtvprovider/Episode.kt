package com.tajmoti.libtvprovider

data class Episode(
    val info: EpisodeInfo,
    val links: List<VideoStreamRef.Unresolved>
)