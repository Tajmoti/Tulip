package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.entity.ItemMapping

class TvShowMappingMapper {

    fun fromDb(db: ItemMapping): TvShowKey.Hosted = with(db) {
        return TvShowKey.Hosted(service, key)
    }

    fun toDb(repo: TvShowKey.Hosted, tmdbKey: TvShowKey.Tmdb): ItemMapping = with(repo) {
        return ItemMapping(streamingService, id, tmdbKey.id)
    }
}