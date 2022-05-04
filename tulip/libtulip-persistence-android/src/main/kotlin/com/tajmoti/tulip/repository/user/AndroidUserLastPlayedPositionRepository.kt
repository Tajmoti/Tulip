package com.tajmoti.tulip.repository.user

import com.tajmoti.libtulip.repository.UserLastPlayedPositionRepository
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.dao.user.PlayingProgressDao
import com.tajmoti.tulip.entity.user.PlayingProgressHostedMovie
import com.tajmoti.tulip.entity.user.PlayingProgressTmdbMovie
import com.tajmoti.tulip.entity.user.PlayingProgressHostedTvShow
import com.tajmoti.tulip.entity.user.PlayingProgressTmdbTvShow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AndroidUserLastPlayedPositionRepository @Inject constructor(
    private val playingProgressDao: PlayingProgressDao
) : UserLastPlayedPositionRepository {


    override fun getLastPlayedPositionForTmdbItem(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is TvShowKey.Tmdb -> playingProgressDao.getLastPlayingPositionTvShowTmdb(key.id)
                .map { it?.fromDb() }
            is MovieKey.Tmdb -> playingProgressDao.getLastPlayingPositionMovieTmdb(key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionForHostedItem(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return when (key) {
            is TvShowKey.Hosted -> playingProgressDao.getLastPlayingPositionHosted(key.streamingService, key.id)
                .map { it?.fromDb() }
            is MovieKey.Hosted -> playingProgressDao.getLastPlayingPositionMovieHosted(key.streamingService, key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is EpisodeKey.Tmdb -> playingProgressDao.getLastPlayingPositionEpisodeTmdb(
                key.seasonKey.tvShowKey.id,
                key.seasonNumber,
                key.episodeNumber
            ).map { it?.fromDb() }
            is MovieKey.Tmdb -> playingProgressDao.getLastPlayingPositionMovieTmdb(key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return when (key) {
            is EpisodeKey.Hosted -> playingProgressDao.getLastPlayingPositionEpisodeHosted(
                key.streamingService,
                key.tvShowKey.id,
                key.seasonKey.seasonNumber,
                key.id
            ).map { it?.fromDb() }
            is MovieKey.Hosted -> playingProgressDao.getLastPlayingPositionMovieHosted(key.streamingService, key.id)
                .map { it?.fromDb() }
        }
    }

    override suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float) {
        return when (key) {
            is EpisodeKey.Tmdb ->
                playingProgressDao.insertLastPlayingPositionTmdb(key.toLastPositionDb(progress))
            is EpisodeKey.Hosted ->
                playingProgressDao.insertLastPlayingPositionHosted(key.toLastPositionDb(progress))
            is MovieKey.Hosted ->
                playingProgressDao.insertLastPlayingPositionMovieHosted(key.toLastPositionDb(progress))
            is MovieKey.Tmdb ->
                playingProgressDao.insertLastPlayingPositionMovieTmdb(key.toLastPositionDb(progress))
        }
    }

    override suspend fun removeLastPlayedPosition(key: StreamableKey) {
        when (key) {
            is EpisodeKey.Tmdb ->
                playingProgressDao.deleteLastPlayingPositionEpisodeTmdb(
                    key.tvShowKey.id,
                    key.seasonNumber,
                    key.episodeNumber
                )
            is EpisodeKey.Hosted ->
                playingProgressDao.deleteLastPlayingPositionEpisodeHosted(
                    key.streamingService,
                    key.tvShowKey.id,
                    key.seasonKey.seasonNumber,
                    key.id
                )
            is MovieKey.Hosted ->
                playingProgressDao.deleteLastPlayingPositionMovieHosted(key.streamingService, key.id)
            is MovieKey.Tmdb ->
                playingProgressDao.deleteLastPlayingPositionMovieTmdb(key.id)
        }
    }


    private fun PlayingProgressTmdbTvShow.fromDb(): LastPlayedPosition.Tmdb {
        val tvShowKey = TvShowKey.Tmdb(tvShowId)
        val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
        val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
        return LastPlayedPosition.Tmdb(key, progress)
    }

    private fun PlayingProgressHostedTvShow.fromDb(): LastPlayedPosition.Hosted {
        val tvShowKey = TvShowKey.Hosted(streamingService, tvShowId)
        val seasonKey = SeasonKey.Hosted(tvShowKey, seasonNumber)
        val key = EpisodeKey.Hosted(seasonKey, episodeId)
        return LastPlayedPosition.Hosted(key, progress)
    }

    private fun PlayingProgressTmdbMovie.fromDb(): LastPlayedPosition.Tmdb {
        val key = MovieKey.Tmdb(movieId)
        return LastPlayedPosition.Tmdb(key, progress)
    }

    private fun PlayingProgressHostedMovie.fromDb(): LastPlayedPosition.Hosted {
        val key = MovieKey.Hosted(streamingService, movieId)
        return LastPlayedPosition.Hosted(key, progress)
    }

    private fun EpisodeKey.Tmdb.toLastPositionDb(progress: Float): PlayingProgressTmdbTvShow {
        return PlayingProgressTmdbTvShow(
            tvShowKey.id,
            seasonNumber,
            episodeNumber,
            progress
        )
    }

    private fun EpisodeKey.Hosted.toLastPositionDb(progress: Float): PlayingProgressHostedTvShow {
        return PlayingProgressHostedTvShow(
            tvShowKey.streamingService,
            tvShowKey.id,
            seasonKey.seasonNumber,
            id,
            progress
        )
    }

    private fun MovieKey.Tmdb.toLastPositionDb(progress: Float): PlayingProgressTmdbMovie {
        return PlayingProgressTmdbMovie(
            id,
            progress
        )
    }

    private fun MovieKey.Hosted.toLastPositionDb(progress: Float): PlayingProgressHostedMovie {
        return PlayingProgressHostedMovie(
            streamingService,
            id,
            progress
        )
    }
}