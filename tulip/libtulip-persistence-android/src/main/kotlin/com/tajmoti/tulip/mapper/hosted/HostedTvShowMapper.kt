package com.tajmoti.tulip.mapper.hosted

import com.tajmoti.libtulip.model.LanguageCode
import com.tajmoti.libtulip.model.Season
import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.entity.hosted.HostedTvShow

class HostedTvShowMapper {

    fun fromDb(db: HostedTvShow, tmdbKey: TvShowKey.Tmdb?, seasons: List<Season.Hosted>): TvShow.Hosted = with(db) {
        val key = TvShowKey.Hosted(service, key)
        return TvShow.Hosted(key, name, LanguageCode(language), firstAirDateYear, tmdbKey, seasons)
    }

    fun toDb(repo: TvShow.Hosted): HostedTvShow = with(repo) {
        return HostedTvShow(key.streamingService, key.id, name, language.code, firstAirDateYear)
    }
}