package com.tajmoti.tulip.db.dao.userdata

import androidx.room.*
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteHostedItem
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteTmdbItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM DbFavoriteTmdbItem")
    suspend fun getAllTmdbFavorites(): List<DbFavoriteTmdbItem>

    @Query("SELECT * FROM DbFavoriteTmdbItem")
    fun getAllTmdbFavoritesAsFlow(): Flow<List<DbFavoriteTmdbItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTmdbFavorite(item: DbFavoriteTmdbItem)

    @Delete
    suspend fun deleteTmdbFavorite(item: DbFavoriteTmdbItem)

    @Query("SELECT * FROM DbFavoriteHostedItem")
    suspend fun getAllHostedFavorites(): List<DbFavoriteHostedItem>

    @Query("SELECT * FROM DbFavoriteHostedItem")
    fun getAllHostedFavoritesAsFlow(): Flow<List<DbFavoriteHostedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostedFavorite(item: DbFavoriteHostedItem)

    @Delete
    suspend fun deleteHostedFavorite(item: DbFavoriteHostedItem)
}
