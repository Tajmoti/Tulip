package com.tajmoti.tulip.dao.tmdb

import androidx.room.*
import com.tajmoti.tulip.entity.tmdb.TmdbTvShow
import kotlinx.coroutines.flow.Flow

@Dao
interface TmdbTvShowDao {

    @Transaction
    @Query("SELECT * FROM TmdbTvShow WHERE id == :tvId LIMIT 1")
    fun getTvShow(tvId: Long): Flow<TmdbTvShow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvShow(tvShow: TmdbTvShow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvShows(tvShows: List<TmdbTvShow>)
}