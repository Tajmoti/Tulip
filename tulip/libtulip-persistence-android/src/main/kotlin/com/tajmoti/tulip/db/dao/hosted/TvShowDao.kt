package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbTvShow
import kotlinx.coroutines.flow.Flow

@Dao
interface TvShowDao {

    @Query("SELECT * FROM DbTvShow WHERE service == :service AND `key` == :key LIMIT 1")
    fun getByKey(service: StreamingService, key: String): Flow<DbTvShow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbTvShow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: List<DbTvShow>)
}
