package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbSeason
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {
    @Query("SELECT * FROM DbSeason WHERE service == :service AND tvShowKey == :tvShowKey")
    fun getForShow(
        service: StreamingService,
        tvShowKey: String
    ): Flow<List<DbSeason>>

    @Query("SELECT * FROM DbSeason WHERE service == :service AND tvShowKey == :tvShowKey AND number == :seasonNumber LIMIT 1")
    fun getBySeasonNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): Flow<DbSeason?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(season: DbSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seasons: List<DbSeason>)
}
