package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.hosted.EpisodeDao
import com.tajmoti.tulip.db.dao.hosted.MovieDao
import com.tajmoti.tulip.db.dao.hosted.SeasonDao
import com.tajmoti.tulip.db.dao.hosted.TvShowDao
import com.tajmoti.tulip.db.entity.hosted.DbEpisode
import com.tajmoti.tulip.db.entity.hosted.DbMovie
import com.tajmoti.tulip.db.entity.hosted.DbSeason
import com.tajmoti.tulip.db.entity.hosted.DbTvShow

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
