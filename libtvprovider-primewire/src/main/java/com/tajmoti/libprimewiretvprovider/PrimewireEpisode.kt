package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.show.Episode

class PrimewireEpisode(
    override val number: Int?,
    override val name: String?,
    baseUrl: String,
    episodeUrl: String,
    pageLoader: SimplePageSourceLoader
) : PrimewireEpisodeOrMovie(baseUrl, episodeUrl, pageLoader), Episode