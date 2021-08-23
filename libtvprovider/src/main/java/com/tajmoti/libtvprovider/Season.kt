package com.tajmoti.libtvprovider

data class Season(
    val tvShowKey: String,
    /**
     * One-based season number
     */
    val number: Int,
    /**
     * All episodes of this season
     */
    val episodes: List<EpisodeInfo>
)