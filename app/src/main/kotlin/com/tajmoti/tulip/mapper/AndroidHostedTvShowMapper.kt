package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.hosted.DbTvShow

class AndroidHostedTvShowMapper {

    fun fromDb(db: DbTvShow, tmdbKey: TvShowKey.Tmdb?, seasons: List<Season.Hosted>): TvShow.Hosted = with(db) {
        val key = TvShowKey.Hosted(service, key)
        return TvShow.Hosted(key, name, LanguageCode(language), firstAirDateYear, tmdbKey, seasons)
    }

    fun toDb(repo: TvShow.Hosted): DbTvShow = with(repo) {
        return DbTvShow(key.streamingService, key.id, name, language.code, firstAirDateYear)
    }
}