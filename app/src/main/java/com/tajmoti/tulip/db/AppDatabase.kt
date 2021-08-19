package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.EpisodeDao
import com.tajmoti.tulip.db.dao.MovieDao
import com.tajmoti.tulip.db.dao.SeasonDao
import com.tajmoti.tulip.db.dao.TvShowDao
import com.tajmoti.tulip.db.entity.DbEpisode
import com.tajmoti.tulip.db.entity.DbMovie
import com.tajmoti.tulip.db.entity.DbSeason
import com.tajmoti.tulip.db.entity.DbTvShow

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
