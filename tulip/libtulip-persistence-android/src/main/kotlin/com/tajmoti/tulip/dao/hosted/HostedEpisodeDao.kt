package com.tajmoti.tulip.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.tulip.entity.hosted.HostedEpisode
import kotlinx.coroutines.flow.Flow

@Dao
interface HostedEpisodeDao {
    @Query("SELECT * FROM HostedEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber ORDER BY number ASC")
    fun getForSeason(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): Flow<List<HostedEpisode>>

    @Query("SELECT * FROM HostedEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber AND `key` == :key LIMIT 1")
    fun getByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        key: String
    ): Flow<HostedEpisode?>

    @Query("SELECT * FROM HostedEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber AND number == :episodeNumber LIMIT 1")
    fun getByNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<HostedEpisode?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episodes: List<HostedEpisode>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: HostedEpisode)
}
