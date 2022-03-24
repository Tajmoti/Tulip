package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.hosted.StreamingService

val SeasonWithEpisodes.seasonNumber: Int
    get() = season.seasonNumber

val TulipItem.Hosted.streamingService: StreamingService
    get() = key.streamingService

val TulipCompleteEpisodeInfo.showName: String
    get() = tvShow.name

val TulipCompleteEpisodeInfo.episodeNumber: Int
    get() = episodeInfo.episodeNumber