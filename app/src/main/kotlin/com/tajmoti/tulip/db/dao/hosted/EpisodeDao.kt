package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbEpisode
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber ORDER BY number ASC")
    fun getForSeason(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): Flow<List<DbEpisode>>

    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber AND `key` == :key LIMIT 1")
    fun getByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        key: String
    ): Flow<DbEpisode?>

    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber AND number == :episodeNumber LIMIT 1")
    fun getByNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<DbEpisode?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episodes: List<DbEpisode>)
}
