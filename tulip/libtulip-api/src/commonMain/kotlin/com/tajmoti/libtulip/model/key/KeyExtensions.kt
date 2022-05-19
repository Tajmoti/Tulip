package com.tajmoti.libtulip.model.key


inline val SeasonKey.Hosted.streamingService: StreamingService
    get() = tvShowKey.streamingService

inline val EpisodeKey.Hosted.tvShowKey: TvShowKey.Hosted
    get() = seasonKey.tvShowKey

inline val EpisodeKey.Tmdb.tvShowKey: TvShowKey.Tmdb
    get() = seasonKey.tvShowKey

inline val EpisodeKey.Tmdb.seasonNumber: Int
    get() = seasonKey.seasonNumber