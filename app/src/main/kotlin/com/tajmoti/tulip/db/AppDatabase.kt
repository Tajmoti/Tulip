package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.hosted.*
import com.tajmoti.tulip.db.entity.hosted.*

@Database(
    entities = [
        DbTvShow::class,
        DbSeason::class,
        DbEpisode::class,
        DbMovie::class,
        DbTmdbMapping::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tvShowDao(): TvShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun movieDao(): MovieDao
    abstract fun tmdbMappingDao(): TmdbMappingDao
}
