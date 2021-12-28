package com.tajmoti.libtvprovider.model

data class Season(
    /**
     * One-based season number
     */
    val number: Int,
    /**
     * All episodes of this season
     */
    val episodes: List<EpisodeInfo>
)