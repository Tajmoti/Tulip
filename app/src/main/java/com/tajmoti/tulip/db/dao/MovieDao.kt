package com.tajmoti.tulip.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.StreamingService
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.entity.DbMovie

@Dao
interface MovieDao {
    @Query("SELECT * FROM DbMovie WHERE service == :service AND key == :key LIMIT 1")
    suspend fun getByKey(service: StreamingService, key: String): DbMovie?

    suspend fun getByKey(key: MovieKey): DbMovie? {
        return getByKey(key.service, key.movieId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbMovie)
}
