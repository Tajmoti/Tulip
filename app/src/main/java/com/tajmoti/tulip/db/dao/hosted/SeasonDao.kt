package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbSeason

@Dao
interface SeasonDao {
    @Query("SELECT * FROM DbSeason WHERE service == :service AND tvShowKey == :tvShowKey")
    suspend fun getForShow(
        service: StreamingService,
        tvShowKey: String
    ): List<DbSeason>

    @Query("SELECT * FROM DbSeason WHERE service == :service AND tvShowKey == :tvShowKey AND number == :seasonNumber LIMIT 1")
    suspend fun getBySeasonNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): DbSeason?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seasons: List<DbSeason>)
}
