package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.dao.userdata.PlayingHistoryDao
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteHostedItem
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteTmdbItem
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTmdb

@Database(
    entities = [
        DbFavoriteTmdbItem::class,
        DbFavoriteHostedItem::class,
        DbLastPlayedPositionTmdb::class,
        DbLastPlayedPositionHosted::class
    ],
    version = 1
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoritesDao
    abstract fun playingHistoryDao(): PlayingHistoryDao
}
