package com.tajmoti.tulip.db.dao.userdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionMovieHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionMovieTmdb
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTvShowHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTvShowTmdb
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayingHistoryDao {
    @Query("SELECT * FROM DbLastPlayedPositionTvShowTmdb WHERE tvShowId = :tvShowId LIMIT 1")
    fun getLastPlayingPositionTvShowTmdb(
        tvShowId: Long
    ): Flow<DbLastPlayedPositionTvShowTmdb?>

    @Query("SELECT * FROM DbLastPlayedPositionTvShowTmdb WHERE tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND :episodeNumber = episodeNumber LIMIT 1")
    fun getLastPlayingPositionEpisodeTmdb(
        tvShowId: Long,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<DbLastPlayedPositionTvShowTmdb?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionTmdb(
        item: DbLastPlayedPositionTvShowTmdb
    )

    @Query("DELETE FROM DbLastPlayedPositionTvShowTmdb WHERE tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
    suspend fun deleteLastPlayingPositionEpisodeTmdb(
        tvShowId: Long,
        seasonNumber: Int,
        episodeNumber: Int
    )


    @Query("SELECT * FROM DbLastPlayedPositionMovieTmdb WHERE movieId = :movieId LIMIT 1")
    fun getLastPlayingPositionMovieTmdb(
        movieId: Long
    ): Flow<DbLastPlayedPositionMovieTmdb?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionMovieTmdb(
        item: DbLastPlayedPositionMovieTmdb
    )

    @Query("DELETE FROM DbLastPlayedPositionMovieTmdb WHERE movieId = :movieId")
    suspend fun deleteLastPlayingPositionMovieTmdb(
        movieId: Long
    )


    @Query("SELECT * FROM DbLastPlayedPositionTvShowHosted WHERE streamingService = :streamingService AND tvShowId = :tvShowId LIMIT 1")
    fun getLastPlayingPositionHosted(
        streamingService: StreamingService,
        tvShowId: String
    ): Flow<DbLastPlayedPositionTvShowHosted?>

    @Query("SELECT * FROM DbLastPlayedPositionTvShowHosted WHERE streamingService = :streamingService AND tvShowId = :tvShowId AND :seasonNumber = seasonNumber AND :episodeId = episodeId LIMIT 1")
    fun getLastPlayingPositionEpisodeHosted(
        streamingService: StreamingService,
        tvShowId: String,
        seasonNumber: Int,
        episodeId: String
    ): Flow<DbLastPlayedPositionTvShowHosted?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionHosted(
        item: DbLastPlayedPositionTvShowHosted
    )

    @Query("DELETE FROM DbLastPlayedPositionTvShowHosted WHERE streamingService = :streamingService AND tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND episodeId = :episodeId")
    suspend fun deleteLastPlayingPositionEpisodeHosted(
        streamingService: StreamingService,
        tvShowId: String,
        seasonNumber: Int,
        episodeId: String
    )


    @Query("SELECT * FROM DbLastPlayedPositionMovieHosted WHERE streamingService = :streamingService AND movieId = :movieId LIMIT 1")
    fun getLastPlayingPositionMovieHosted(
        streamingService: StreamingService,
        movieId: String
    ): Flow<DbLastPlayedPositionMovieHosted?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionMovieHosted(
        item: DbLastPlayedPositionMovieHosted
    )

    @Query("DELETE FROM DbLastPlayedPositionMovieHosted WHERE streamingService = :streamingService AND movieId = :movieId")
    suspend fun deleteLastPlayingPositionMovieHosted(
        streamingService: StreamingService,
        movieId: String
    )
}
