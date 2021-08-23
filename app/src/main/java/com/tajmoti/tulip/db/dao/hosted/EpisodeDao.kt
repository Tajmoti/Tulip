package com.tajmoti.tulip.db.dao.hosted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.tulip.db.entity.hosted.DbEpisode

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber")
    suspend fun getForSeason(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int
    ): List<DbEpisode>

    suspend fun getForSeason(seasonKey: SeasonKey.Hosted): List<DbEpisode> {
        return getForSeason(seasonKey.service, seasonKey.tvShowId, seasonKey.seasonNumber)
    }

    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber AND key == :key LIMIT 1")
    suspend fun getByKey(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        key: String
    ): DbEpisode?

    @Query("SELECT * FROM DbEpisode WHERE service == :service AND tvShowKey == :tvShowKey AND seasonNumber == :seasonNumber AND number == :episodeNumber LIMIT 1")
    suspend fun getByNumber(
        service: StreamingService,
        tvShowKey: String,
        seasonNumber: Int,
        episodeNumber: Int
    ): DbEpisode?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: DbEpisode)
}
