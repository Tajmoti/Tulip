package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.tulip.db.entity.hosted.DbMovie

@Dao
interface MovieDao {
    @Query("SELECT * FROM DbMovie WHERE service == :service AND key == :key LIMIT 1")
    suspend fun getByKey(service: StreamingService, key: String): DbMovie?

    suspend fun getByKey(key: MovieKey.Hosted): DbMovie? {
        return getByKey(key.service, key.movieId)
    }

    @Query("SELECT * FROM DbMovie WHERE tmdbId == :tmdbId")
    suspend fun getByTmdbId(tmdbId: Long): List<DbMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbMovie)
}
