package com.tajmoti.libtvprovider.model

data class EpisodeInfo(
    /**
     * Key of this episode
     */
    val key: String,
    /**
     * Episode number or 0 if it's a special
     */
    val number: Int,
    /**
     * Name of the episode or null if unknown
     */
    val name: String?,
    /**
     * Episode overview or null if none
     */
    val overview: String?
)