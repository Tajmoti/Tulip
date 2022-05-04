package com.tajmoti.tulip.dao.tmdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.entity.tmdb.TmdbEpisode
import kotlinx.coroutines.flow.Flow

@Dao
interface TmdbEpisodeDao {

    @Query("SELECT * FROM TmdbEpisode WHERE tvId == :tvId AND seasonNumber == :seasonNumber AND episodeNumber == :episodeNumber LIMIT 1")
    fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Flow<TmdbEpisode?>

    @Query("SELECT * FROM TmdbEpisode WHERE tvId == :tvId AND seasonNumber == :seasonNumber")
    fun getEpisodes(tvId: Long, seasonNumber: Int): Flow<List<TmdbEpisode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: TmdbEpisode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<TmdbEpisode>)
}