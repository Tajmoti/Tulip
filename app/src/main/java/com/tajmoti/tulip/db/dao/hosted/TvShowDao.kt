package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.hosted.DbTvShow

@Dao
interface TvShowDao {

    @Query("SELECT * FROM DbTvShow WHERE service == :service AND `key` == :key LIMIT 1")
    suspend fun getByKey(service: StreamingService, key: String): DbTvShow?

    @Query("SELECT * FROM DbTvShow INNER JOIN DbTmdbMapping mapping ON mapping.`key` == DbTvShow.`key` WHERE tmdbId == :tmdbId")
    suspend fun getByTmdbId(tmdbId: Long): List<DbTvShow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbTvShow)
}
