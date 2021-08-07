package com.tajmoti.libtvprovider.kinox

import com.tajmoti.libtvprovider.Season

data class KinoxSeason(
    override val number: Int,
    override val episodes: List<KinoxEpisode>
) : Season {
    override val key = number.toString()
}