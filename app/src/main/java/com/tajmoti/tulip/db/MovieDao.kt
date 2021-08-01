package com.tajmoti.tulip.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.model.DbMovie
import com.tajmoti.tulip.model.StreamingService

@Dao
interface MovieDao {
    @Query("SELECT * FROM DbMovie WHERE service == :service AND key == :key LIMIT 1")
    suspend fun getByKey(service: StreamingService, key: String): DbMovie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbMovie)
}
