package com.tajmoti.tulip.repository

import com.tajmoti.libtulip.data.UserLastPlayedPositionRepository
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.db.dao.userdata.PlayingHistoryDao
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionMovieHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionMovieTmdb
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTvShowHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTvShowTmdb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidUserLastPlayedPositionRepository @Inject constructor(
    private val playingHistoryDao: PlayingHistoryDao
) : UserLastPlayedPositionRepository {


    override fun getLastPlayedPositionForTmdbItem(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is TvShowKey.Tmdb -> playingHistoryDao.getLastPlayingPositionTvShowTmdb(key.id)
                .map { it?.fromDb() }
            is MovieKey.Tmdb -> playingHistoryDao.getLastPlayingPositionMovieTmdb(key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionForHostedItem(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return when (key) {
            is TvShowKey.Hosted -> playingHistoryDao.getLastPlayingPositionHosted(key.streamingService, key.id)
                .map { it?.fromDb() }
            is MovieKey.Hosted -> playingHistoryDao.getLastPlayingPositionMovieHosted(key.streamingService, key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is EpisodeKey.Tmdb -> playingHistoryDao.getLastPlayingPositionEpisodeTmdb(
                key.seasonKey.tvShowKey.id,
                key.seasonNumber,
                key.episodeNumber
            ).map { it?.fromDb() }
            is MovieKey.Tmdb -> playingHistoryDao.getLastPlayingPositionMovieTmdb(key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return when (key) {
            is EpisodeKey.Hosted -> playingHistoryDao.getLastPlayingPositionEpisodeHosted(
                key.streamingService,
                key.tvShowKey.id,
                key.seasonKey.seasonNumber,
                key.id
            ).map { it?.fromDb() }
            is MovieKey.Hosted -> playingHistoryDao.getLastPlayingPositionMovieHosted(key.streamingService, key.id)
                .map { it?.fromDb() }
        }
    }

    override suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float) {
        return when (key) {
            is EpisodeKey.Tmdb ->
                playingHistoryDao.insertLastPlayingPositionTmdb(key.toLastPositionDb(progress))
            is EpisodeKey.Hosted ->
                playingHistoryDao.insertLastPlayingPositionHosted(key.toLastPositionDb(progress))
            is MovieKey.Hosted ->
                playingHistoryDao.insertLastPlayingPositionMovieHosted(key.toLastPositionDb(progress))
            is MovieKey.Tmdb ->
                playingHistoryDao.insertLastPlayingPositionMovieTmdb(key.toLastPositionDb(progress))
        }
    }

    override suspend fun removeLastPlayedPosition(key: StreamableKey) {
        when (key) {
            is EpisodeKey.Tmdb ->
                playingHistoryDao.deleteLastPlayingPositionEpisodeTmdb(
                    key.tvShowKey.id,
                    key.seasonNumber,
                    key.episodeNumber
                )
            is EpisodeKey.Hosted ->
                playingHistoryDao.deleteLastPlayingPositionEpisodeHosted(
                    key.streamingService,
                    key.tvShowKey.id,
                    key.seasonKey.seasonNumber,
                    key.id
                )
            is MovieKey.Hosted ->
                playingHistoryDao.deleteLastPlayingPositionMovieHosted(key.streamingService, key.id)
            is MovieKey.Tmdb ->
                playingHistoryDao.deleteLastPlayingPositionMovieTmdb(key.id)
        }
    }


    private fun DbLastPlayedPositionTvShowTmdb.fromDb(): LastPlayedPosition.Tmdb {
        val tvShowKey = TvShowKey.Tmdb(tvShowId)
        val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
        return LastPlayedPosition.Tmdb(key, progress)
    }

    private fun DbLastPlayedPositionTvShowHosted.fromDb(): LastPlayedPosition.Hosted {
        val tvShowKey = TvShowKey.Hosted(streamingService, tvShowId)
        val seasonKey = SeasonKey.Hosted(tvShowKey, seasonNumber)
        val key = EpisodeKey.Hosted(seasonKey, episodeId)
        return LastPlayedPosition.Hosted(key, progress)
    }

    private fun DbLastPlayedPositionMovieTmdb.fromDb(): LastPlayedPosition.Tmdb {
        val key = MovieKey.Tmdb(movieId)
        return LastPlayedPosition.Tmdb(key, progress)
    }

    private fun DbLastPlayedPositionMovieHosted.fromDb(): LastPlayedPosition.Hosted {
        val key = MovieKey.Hosted(streamingService, movieId)
        return LastPlayedPosition.Hosted(key, progress)
    }

    private fun EpisodeKey.Tmdb.toLastPositionDb(progress: Float): DbLastPlayedPositionTvShowTmdb {
        return DbLastPlayedPositionTvShowTmdb(
            tvShowKey.id,
            seasonNumber,
            episodeNumber,
            progress
        )
    }

    private fun EpisodeKey.Hosted.toLastPositionDb(progress: Float): DbLastPlayedPositionTvShowHosted {
        return DbLastPlayedPositionTvShowHosted(
            tvShowKey.streamingService,
            tvShowKey.id,
            seasonKey.seasonNumber,
            id,
            progress
        )
    }

    private fun MovieKey.Tmdb.toLastPositionDb(progress: Float): DbLastPlayedPositionMovieTmdb {
        return DbLastPlayedPositionMovieTmdb(
            id,
            progress
        )
    }

    private fun MovieKey.Hosted.toLastPositionDb(progress: Float): DbLastPlayedPositionMovieHosted {
        return DbLastPlayedPositionMovieHosted(
            streamingService,
            id,
            progress
        )
    }
}