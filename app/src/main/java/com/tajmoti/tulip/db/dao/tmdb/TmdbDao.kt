package com.tajmoti.tulip.db.dao.tmdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbEpisode
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbMovie
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbTv

@Dao
interface TmdbDao {

    @Query("SELECT * FROM DbTmdbTv WHERE id == :tvId LIMIT 1")
    suspend fun getTv(tvId: Long): DbTmdbTv?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTv(tv: DbTmdbTv)

    @Query("SELECT * FROM DbTmdbSeason WHERE tvId == :tvId AND seasonNumber == :seasonNumber LIMIT 1")
    suspend fun getSeason(tvId: Long, seasonNumber: Int): DbTmdbSeason?

    @Query("SELECT * FROM DbTmdbSeason WHERE tvId == :tvId")
    suspend fun getSeasons(tvId: Long): List<DbTmdbSeason>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: DbTmdbSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(season: List<DbTmdbSeason>)

    @Query("SELECT * FROM DbTmdbEpisode WHERE tvId == :tvId AND seasonNumber == :seasonNumber AND episodeNumber == :episodeNumber LIMIT 1")
    suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): DbTmdbEpisode?

    @Query("SELECT * FROM DbTmdbEpisode WHERE tvId == :tvId AND seasonNumber == :seasonNumber")
    suspend fun getEpisodes(tvId: Long, seasonNumber: Int): List<DbTmdbEpisode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: DbTmdbEpisode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<DbTmdbEpisode>)

    @Query("SELECT * FROM DbTmdbMovie WHERE id == :id")
    suspend fun getMovie(id: Long): DbTmdbMovie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: DbTmdbMovie)
}
