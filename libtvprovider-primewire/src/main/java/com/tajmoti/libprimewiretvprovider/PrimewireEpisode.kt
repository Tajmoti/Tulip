package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.Episode

class PrimewireEpisode(
    override val number: Int?,
    override val name: String?,
    baseUrl: String,
    episodeUrl: String,
    pageLoader: SimplePageSourceLoader
) : PrimewireEpisodeOrMovie(baseUrl, episodeUrl, pageLoader), Episode {

    override fun toString(): String {
        return "PrimewireEpisode(number=$number, name=$name)"
    }
}