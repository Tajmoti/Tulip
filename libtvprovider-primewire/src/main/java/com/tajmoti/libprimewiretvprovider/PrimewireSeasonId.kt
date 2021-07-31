package com.tajmoti.libprimewiretvprovider

import java.io.Serializable

data class PrimewireSeasonId(
    val number: Int,
    val episodes: List<EpisodeInfo>
) : Serializable {

    data class EpisodeInfo(
        val name: String,
        val url: String
    ) : Serializable
}