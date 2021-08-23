package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.tmdb.TmdbDao
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbEpisode
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbMovie
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbTv

@Database(
    entities = [
        DbTmdbTv::class,
        DbTmdbSeason::class,
        DbTmdbEpisode::class,
        DbTmdbMovie::class
    ],
    version = 1
)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun tmdbDao(): TmdbDao
}
