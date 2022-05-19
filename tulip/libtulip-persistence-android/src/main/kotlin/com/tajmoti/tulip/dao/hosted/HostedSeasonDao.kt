package com.tajmoti.tulip.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.tulip.entity.hosted.HostedSeason
import kotlinx.coroutines.flow.Flow

@Dao
interface HostedSeasonDao {
    @Query("SELECT * FROM HostedSeason WHERE service == :service AND tvShowKey == :tvShowKey")
    fun getForShow(
        service: StreamingService,
        tvShowKey: String
    ): Flow<List<HostedSeason>>

    @Query("SELECT * FROM HostedSeason WHERE service == :service AND tvShowKey == :tvShowKey AND number == :seasonNumber LIMIT 1")
    fun getBySeasonNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): Flow<HostedSeason?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(season: HostedSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seasons: List<HostedSeason>)
}
