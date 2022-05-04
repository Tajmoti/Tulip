package com.tajmoti.tulip.dao.tmdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.entity.tmdb.TmdbMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface TmdbMovieDao {

    @Query("SELECT * FROM TmdbMovie WHERE id == :id")
    fun getMovie(id: Long): Flow<TmdbMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: TmdbMovie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<TmdbMovie>)
}