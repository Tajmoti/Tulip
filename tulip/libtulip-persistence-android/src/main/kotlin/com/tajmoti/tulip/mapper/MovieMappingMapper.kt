package com.tajmoti.tulip.mapper

import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.entity.ItemMapping

class MovieMappingMapper {

    fun fromDb(db: ItemMapping): MovieKey.Hosted = with(db) {
        return MovieKey.Hosted(service, key)
    }

    fun toDb(repo: MovieKey.Hosted, tmdbKey: MovieKey.Tmdb): ItemMapping = with(repo) {
        return ItemMapping(streamingService, id, tmdbKey.id)
    }
}