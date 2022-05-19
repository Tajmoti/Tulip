package com.tajmoti.tulip.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.tulip.entity.ItemMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemMappingDao {

    @Query("SELECT * FROM ItemMapping WHERE service == :service AND `key` == :key LIMIT 1")
    fun getTmdbIdByHostedKey(service: StreamingService, key: String): Flow<ItemMapping?>

    @Query("SELECT * FROM ItemMapping WHERE tmdbId == :tmdbId")
    fun getHostedKeysByTmdbId(tmdbId: Long): Flow<List<ItemMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: ItemMapping)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episodes: List<ItemMapping>)
}
