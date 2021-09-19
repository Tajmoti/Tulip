package com.tajmoti.tulip.db.dao.userdata

import androidx.room.*
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.info.ItemType
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteHostedItem
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteTmdbItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT EXISTS(SELECT * FROM DbFavoriteTmdbItem WHERE type = :type AND tmdbItemId = :tmdbItemId)")
    fun isTmdbFavorite(type: ItemType, tmdbItemId: Long): Flow<Boolean>

    @Query("SELECT * FROM DbFavoriteTmdbItem")
    suspend fun getAllTmdbFavorites(): List<DbFavoriteTmdbItem>

    @Query("SELECT * FROM DbFavoriteTmdbItem")
    fun getAllTmdbFavoritesAsFlow(): Flow<List<DbFavoriteTmdbItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTmdbFavorite(item: DbFavoriteTmdbItem)

    @Delete
    suspend fun deleteTmdbFavorite(item: DbFavoriteTmdbItem)


    @Query("SELECT EXISTS(SELECT * FROM DbFavoriteHostedItem WHERE type = :type AND streamingService = :streamingService AND key == :key)")
    fun isHostedFavorite(type: ItemType, streamingService: StreamingService, key: String): Flow<Boolean>

    @Query("SELECT * FROM DbFavoriteHostedItem")
    suspend fun getAllHostedFavorites(): List<DbFavoriteHostedItem>

    @Query("SELECT * FROM DbFavoriteHostedItem")
    fun getAllHostedFavoritesAsFlow(): Flow<List<DbFavoriteHostedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostedFavorite(item: DbFavoriteHostedItem)

    @Delete
    suspend fun deleteHostedFavorite(item: DbFavoriteHostedItem)
}
