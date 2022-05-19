package com.tajmoti.tulip.dao.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.key.StreamingService
import com.tajmoti.tulip.entity.user.PlayingProgressHostedMovie
import com.tajmoti.tulip.entity.user.PlayingProgressHostedTvShow
import com.tajmoti.tulip.entity.user.PlayingProgressTmdbMovie
import com.tajmoti.tulip.entity.user.PlayingProgressTmdbTvShow
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayingProgressDao {
    @Query("SELECT * FROM PlayingProgressTmdbTvShow WHERE tvShowId = :tvShowId LIMIT 1")
    fun getLastPlayingPositionTvShowTmdb(
        tvShowId: Long
    ): Flow<PlayingProgressTmdbTvShow?>

    @Query("SELECT * FROM PlayingProgressTmdbTvShow WHERE tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND :episodeNumber = episodeNumber LIMIT 1")
    fun getLastPlayingPositionEpisodeTmdb(
        tvShowId: Long,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<PlayingProgressTmdbTvShow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionTmdb(
        item: PlayingProgressTmdbTvShow
    )

    @Query("DELETE FROM PlayingProgressTmdbTvShow WHERE tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
    suspend fun deleteLastPlayingPositionEpisodeTmdb(
        tvShowId: Long,
        seasonNumber: Int,
        episodeNumber: Int
    )


    @Query("SELECT * FROM PlayingProgressTmdbMovie WHERE movieId = :movieId LIMIT 1")
    fun getLastPlayingPositionMovieTmdb(
        movieId: Long
    ): Flow<PlayingProgressTmdbMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionMovieTmdb(
        item: PlayingProgressTmdbMovie
    )

    @Query("DELETE FROM PlayingProgressTmdbMovie WHERE movieId = :movieId")
    suspend fun deleteLastPlayingPositionMovieTmdb(
        movieId: Long
    )


    @Query("SELECT * FROM PlayingProgressHostedTvShow WHERE streamingService = :streamingService AND tvShowId = :tvShowId LIMIT 1")
    fun getLastPlayingPositionHosted(
        streamingService: StreamingService,
        tvShowId: String
    ): Flow<PlayingProgressHostedTvShow?>

    @Query("SELECT * FROM PlayingProgressHostedTvShow WHERE streamingService = :streamingService AND tvShowId = :tvShowId AND :seasonNumber = seasonNumber AND :episodeId = episodeId LIMIT 1")
    fun getLastPlayingPositionEpisodeHosted(
        streamingService: StreamingService,
        tvShowId: String,
        seasonNumber: Int,
        episodeId: String
    ): Flow<PlayingProgressHostedTvShow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionHosted(
        item: PlayingProgressHostedTvShow
    )

    @Query("DELETE FROM PlayingProgressHostedTvShow WHERE streamingService = :streamingService AND tvShowId = :tvShowId AND seasonNumber = :seasonNumber AND episodeId = :episodeId")
    suspend fun deleteLastPlayingPositionEpisodeHosted(
        streamingService: StreamingService,
        tvShowId: String,
        seasonNumber: Int,
        episodeId: String
    )


    @Query("SELECT * FROM PlayingProgressHostedMovie WHERE streamingService = :streamingService AND movieId = :movieId LIMIT 1")
    fun getLastPlayingPositionMovieHosted(
        streamingService: StreamingService,
        movieId: String
    ): Flow<PlayingProgressHostedMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayingPositionMovieHosted(
        item: PlayingProgressHostedMovie
    )

    @Query("DELETE FROM PlayingProgressHostedMovie WHERE streamingService = :streamingService AND movieId = :movieId")
    suspend fun deleteLastPlayingPositionMovieHosted(
        streamingService: StreamingService,
        movieId: String
    )
}
