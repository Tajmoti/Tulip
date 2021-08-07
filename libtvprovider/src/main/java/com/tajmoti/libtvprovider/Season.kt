package com.tajmoti.libtvprovider

interface Season : Marshallable {
    /**
     * One-based season number
     */
    val number: Int

    val episodes: List<Episode>

    data class Info(
        override val key: String,
        val number: Int,
        val episodes: List<Episode.Info>
    ) : Marshallable
}