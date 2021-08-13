package com.tajmoti.tulip.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.model.DbTvShow
import com.tajmoti.tulip.model.StreamingService
import com.tajmoti.tulip.model.key.TvShowKey

@Dao
interface TvShowDao {
    @Query("SELECT * FROM DbTvShow WHERE service == :service AND key == :key LIMIT 1")
    suspend fun getByKey(service: StreamingService, key: String): DbTvShow?

    suspend fun getByKey(key: TvShowKey): DbTvShow? {
        return getByKey(key.service, key.tvShowId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbTvShow)
}
