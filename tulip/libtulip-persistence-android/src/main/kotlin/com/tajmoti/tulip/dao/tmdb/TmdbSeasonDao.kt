package com.tajmoti.tulip.dao.tmdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.entity.tmdb.TmdbSeason
import kotlinx.coroutines.flow.Flow

@Dao
interface TmdbSeasonDao {

    @Query("SELECT * FROM TmdbSeason WHERE tvId == :tvId AND seasonNumber == :seasonNumber LIMIT 1")
    fun getSeason(tvId: Long, seasonNumber: Int): Flow<TmdbSeason?>

    @Query("SELECT * FROM TmdbSeason WHERE tvId == :tvId")
    fun getSeasons(tvId: Long): Flow<List<TmdbSeason>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: TmdbSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(season: List<TmdbSeason>)
}