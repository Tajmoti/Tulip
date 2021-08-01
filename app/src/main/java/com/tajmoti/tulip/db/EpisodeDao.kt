package com.tajmoti.tulip.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.model.DbEpisode
import com.tajmoti.tulip.model.StreamingService

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonKey == :seasonKey")
    suspend fun getForSeason(
        service: StreamingService,
        tvShowKey: String,
        seasonKey: String
    ): List<DbEpisode>

    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonKey == :seasonKey AND key == :key LIMIT 1")
    suspend fun getByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonKey: String,
        key: String
    ): DbEpisode?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbEpisode)
}
