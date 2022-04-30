package com.tajmoti.tulip.db.dao.tmdb

import androidx.room.*
import com.tajmoti.tulip.db.entity.tmdb.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TmdbDao {

    @Transaction
    @Query("SELECT * FROM DbTmdbTv WHERE id == :tvId LIMIT 1")
    fun getTv(tvId: Long): Flow<DbTmdbTvWithSeasons?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTv(tv: DbTmdbTv)

    @Query("SELECT * FROM DbTmdbSeason WHERE tvId == :tvId AND seasonNumber == :seasonNumber LIMIT 1")
    fun getSeason(tvId: Long, seasonNumber: Int): Flow<DbTmdbSeason?>

    @Query("SELECT * FROM DbTmdbSeason WHERE tvId == :tvId")
    fun getSeasons(tvId: Long): Flow<List<DbTmdbSeason>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: DbTmdbSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(season: List<DbTmdbSeason>)

    @Query("SELECT * FROM DbTmdbEpisode WHERE tvId == :tvId AND seasonNumber == :seasonNumber AND episodeNumber == :episodeNumber LIMIT 1")
    fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Flow<DbTmdbEpisode?>

    @Query("SELECT * FROM DbTmdbEpisode WHERE tvId == :tvId AND seasonNumber == :seasonNumber")
    fun getEpisodes(tvId: Long, seasonNumber: Int): Flow<List<DbTmdbEpisode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: DbTmdbEpisode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<DbTmdbEpisode>)

    @Query("SELECT * FROM DbTmdbMovie WHERE id == :id")
    fun getMovie(id: Long): Flow<DbTmdbMovie?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: DbTmdbMovie)
}
