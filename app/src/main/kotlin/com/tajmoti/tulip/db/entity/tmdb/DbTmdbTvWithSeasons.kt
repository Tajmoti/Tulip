package com.tajmoti.tulip.db.entity.tmdb

import androidx.room.Embedded
import androidx.room.Relation

data class DbTmdbTvWithSeasons(
    @Embedded
    val tvShow: DbTmdbTv,
    @Relation(parentColumn = "id", entityColumn = "tvId")
    var seasons: List<DbTmdbSeason>
)