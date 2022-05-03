package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM DbMovie WHERE service == :service AND `key` == :key LIMIT 1")
    fun getByKey(service: StreamingService, key: String): Flow<DbMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: DbMovie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movies: List<DbMovie>)
}
