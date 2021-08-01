package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.model.DbEpisode
import com.tajmoti.tulip.model.DbMovie
import com.tajmoti.tulip.model.DbSeason
import com.tajmoti.tulip.model.DbTvShow

@Database(
    entities = [
        DbTvShow::class,
        DbSeason::class,
        DbEpisode::class,
        DbMovie::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tvShowDao(): TvShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun movieDao(): MovieDao
}
