package com.tajmoti.tulip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteHostedItem
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteTmdbItem

@Database(
    entities = [DbFavoriteTmdbItem::class, DbFavoriteHostedItem::class],
    version = 1
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoritesDao
}
