package com.tajmoti.tulip.dao.user

import androidx.room.*
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.entity.user.FavoriteHostedItem
import com.tajmoti.tulip.entity.user.FavoriteTmdbItem
import com.tajmoti.tulip.entity.user.ItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT EXISTS(SELECT * FROM FavoriteTmdbItem WHERE type = :type AND tmdbItemId = :tmdbItemId)")
    fun isTmdbFavorite(type: ItemType, tmdbItemId: Long): Flow<Boolean>

    @Query("SELECT * FROM FavoriteTmdbItem")
    fun getAllTmdbFavorites(): Flow<List<FavoriteTmdbItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTmdbFavorite(item: FavoriteTmdbItem)

    @Delete
    suspend fun deleteTmdbFavorite(item: FavoriteTmdbItem)


    @Query("SELECT EXISTS(SELECT * FROM FavoriteHostedItem WHERE type = :type AND streamingService = :streamingService AND `key` == :key)")
    fun isHostedFavorite(type: ItemType, streamingService: StreamingService, key: String): Flow<Boolean>

    @Query("SELECT * FROM FavoriteHostedItem")
    fun getAllHostedFavorites(): Flow<List<FavoriteHostedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostedFavorite(item: FavoriteHostedItem)

    @Delete
    suspend fun deleteHostedFavorite(item: FavoriteHostedItem)
}
