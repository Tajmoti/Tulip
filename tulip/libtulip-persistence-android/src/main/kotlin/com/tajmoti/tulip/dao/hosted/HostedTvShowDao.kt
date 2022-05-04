package com.tajmoti.tulip.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.entity.hosted.HostedTvShow
import kotlinx.coroutines.flow.Flow

@Dao
interface HostedTvShowDao {

    @Query("SELECT * FROM HostedTvShow WHERE service == :service AND `key` == :key LIMIT 1")
    fun getByKey(service: StreamingService, key: String): Flow<HostedTvShow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: HostedTvShow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: List<HostedTvShow>)
}
