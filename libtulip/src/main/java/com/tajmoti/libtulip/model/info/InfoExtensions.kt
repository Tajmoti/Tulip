package com.tajmoti.libtulip.model.info

import com.tajmoti.libtulip.model.hosted.StreamingService

val TulipSeasonInfo.seasonNumber: Int
    get() = key.seasonNumber

val TulipItem.Hosted.streamingService: StreamingService
    get() = key.streamingService