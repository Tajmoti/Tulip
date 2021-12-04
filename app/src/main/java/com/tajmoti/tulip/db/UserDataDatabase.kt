package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.dao.userdata.PlayingHistoryDao
import com.tajmoti.tulip.db.entity.userdata.*

@Database(
    entities = [
        DbFavoriteTmdbItem::class,
        DbFavoriteHostedItem::class,
        DbLastPlayedPositionTvShowTmdb::class,
        DbLastPlayedPositionTvShowHosted::class,
        DbLastPlayedPositionMovieTmdb::class,
        DbLastPlayedPositionMovieHosted::class
    ],
    version = 1
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoritesDao
    abstract fun playingHistoryDao(): PlayingHistoryDao
}
