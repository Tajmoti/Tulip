package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.show.Season

data class PrimewireSeason(
    override val number: Int,
    override val episodes: List<PrimewireEpisodeOrMovie>
) : Season {
    override val key = PrimewireSeasonId(
        number,
        episodes.map { PrimewireSeasonId.EpisodeInfo(it.name, it.episodeUrl) }
    )
}