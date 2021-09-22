package com.tajmoti.tulip.db.dao.userdata

import androidx.room.*
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTmdb
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayingHistoryDao {
    @Query("SELECT * FROM DbLastPlayedPositionTmdb WHERE tvShowId = :tvShowId LIMIT 1")
    fun getLastPlayingPositionTmdb(
        tvShowId: Long
    ): Flow<DbLastPlayedPositionTmdb?>

    @Query("SELECT * FROM DbLastPlayedPositionTmdb WHERE tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND :episodeNumber = episodeNumber LIMIT 1")
    fun getLastPlayingPositionEpisodeTmdb(
        tvShowId: Long,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<DbLastPlayedPositionTmdb?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionTmdb(item: DbLastPlayedPositionTmdb)

    @Delete
    suspend fun deleteLastPlayingPositionTmdb(item: DbLastPlayedPositionTmdb)


    @Query("SELECT * FROM DbLastPlayedPositionHosted WHERE streamingService = :streamingService AND tvShowId = :tvShowId LIMIT 1")
    fun getLastPlayingPositionHosted(
        streamingService: StreamingService,
        tvShowId: String
    ): Flow<DbLastPlayedPositionHosted?>

    @Query("SELECT * FROM DbLastPlayedPositionHosted WHERE streamingService = :streamingService AND tvShowId = :tvShowId AND :seasonNumber = seasonNumber AND :episodeId = episodeId LIMIT 1")
    fun getLastPlayingPositionEpisodeHosted(
        streamingService: StreamingService,
        tvShowId: String,
        seasonNumber: Int,
        episodeId: String
    ): Flow<DbLastPlayedPositionHosted?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionHosted(item: DbLastPlayedPositionHosted)

    @Delete
    suspend fun deleteLastPlayingPosition(item: DbLastPlayedPositionHosted)
}
