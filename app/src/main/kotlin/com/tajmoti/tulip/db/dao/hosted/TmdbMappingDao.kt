package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbTmdbMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface TmdbMappingDao {

    @Query("SELECT * FROM DbTmdbMapping WHERE service == :service AND `key` == :key LIMIT 1")
    fun getTmdbIdByHostedKey(service: StreamingService, key: String): Flow<DbTmdbMapping?>

    @Query("SELECT * FROM DbTmdbMapping WHERE tmdbId == :tmdbId")
    fun getHostedKeysByTmdbId(tmdbId: Long): Flow<List<DbTmdbMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbTmdbMapping)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episodes: List<DbTmdbMapping>)
}
