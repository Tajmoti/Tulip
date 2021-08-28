package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.db.entity.hosted.DbTvShow
import kotlinx.coroutines.flow.Flow

@Dao
interface TvShowDao {
    @Query("SELECT * FROM DbTvShow WHERE service == :service AND key == :key LIMIT 1")
    fun getByKeyAsFlow(service: StreamingService, key: String): Flow<DbTvShow?>

    @Query("SELECT * FROM DbTvShow WHERE service == :service AND key == :key LIMIT 1")
    suspend fun getByKey(service: StreamingService, key: String): DbTvShow?

    @Query("SELECT * FROM DbTvShow WHERE tmdbId == :tmdbId")
    suspend fun getByTmdbId(tmdbId: Long): List<DbTvShow>

    suspend fun getByKey(key: TvShowKey.Hosted): DbTvShow? {
        return getByKey(key.streamingService, key.tvShowId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbTvShow)
}
