package com.tajmoti.tulip.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.model.DbSeason
import com.tajmoti.tulip.model.key.SeasonKey
import com.tajmoti.tulip.model.StreamingService

@Dao
interface SeasonDao {
    @Query("SELECT * FROM DbSeason WHERE service == :service AND tvShowKey == :tvShowKey AND key == :key LIMIT 1")
    suspend fun getForShow(
        service: StreamingService,
        tvShowKey: String,
        key: String
    ): DbSeason?

    suspend fun getSeason(key: SeasonKey): DbSeason? {
        return getForShow(key.service, key.tvShowId, key.seasonId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbSeason)
}
