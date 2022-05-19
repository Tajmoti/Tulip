package com.tajmoti.tulip.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.tulip.entity.hosted.HostedMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface HostedMovieDao {
    @Query("SELECT * FROM HostedMovie WHERE service == :service AND `key` == :key LIMIT 1")
    fun getByKey(service: StreamingService, key: String): Flow<HostedMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: HostedMovie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movies: List<HostedMovie>)
}
