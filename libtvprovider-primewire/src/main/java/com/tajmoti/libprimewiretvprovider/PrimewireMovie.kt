package com.tajmoti.libprimewiretvprovider

import com.tajmoti.libtvprovider.TvItem

class PrimewireMovie(
    override val name: String,
    baseUrl: String,
    episodeUrl: String,
    override val firstAirDateYear: Int?,
    pageLoader: SimplePageSourceLoader
) : PrimewireEpisodeOrMovie(baseUrl, episodeUrl, pageLoader), TvItem.Movie {
    override val language = "en"
}