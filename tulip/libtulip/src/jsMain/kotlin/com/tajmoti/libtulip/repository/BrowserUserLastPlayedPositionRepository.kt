package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.HostedEpisodeProgress
import com.tajmoti.libtulip.model.TmdbEpisodeProgress
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserUserLastPlayedPositionRepository : UserLastPlayedPositionRepository {
    private val playingPositionsTmdbTvShow = BrowserStorage<TvShowKey.Tmdb, TmdbEpisodeProgress>(0)
    private val playingPositionsTmdbMovie = BrowserStorage<MovieKey.Tmdb, Float>(1)
    private val playingPositionsHostedTvShow = BrowserStorage<TvShowKey.Hosted, HostedEpisodeProgress>(2)
    private val playingPositionsHostedMovie = BrowserStorage<MovieKey.Hosted, Float>(3)

    override fun getLastPlayedPositionForTmdbItem(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is TvShowKey.Tmdb -> playingPositionsTmdbTvShow.get(key)
                .map { it?.let { LastPlayedPosition.Tmdb(it.key, it.progress) } }
            is MovieKey.Tmdb -> playingPositionsTmdbMovie.get(key)
                .map { it?.let { LastPlayedPosition.Tmdb(key, it) } }
        }
    }

    override fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return getLastPlayedPositionForTmdbItem(key.itemKey).map { it?.takeIf { it.key == key } }
    }

    override fun getLastPlayedPositionForHostedItem(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return when (key) {
            is TvShowKey.Hosted -> playingPositionsHostedTvShow.get(key)
                .map { it?.let { LastPlayedPosition.Hosted(it.key, it.progress) } }
            is MovieKey.Hosted -> playingPositionsHostedMovie.get(key)
                .map { it?.let { LastPlayedPosition.Hosted(key, it) } }
        }
    }

    override fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return getLastPlayedPositionForHostedItem(key.itemKey).map { it?.takeIf { it.key == key } }
    }

    override suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float) {
        logger.warn { "Updating position of $key to $progress" }
        when (key) {
            is EpisodeKey.Tmdb -> playingPositionsTmdbTvShow.put(key.itemKey, TmdbEpisodeProgress(key, progress))
            is EpisodeKey.Hosted -> playingPositionsHostedTvShow.put(key.itemKey, HostedEpisodeProgress(key, progress))
            is MovieKey.Tmdb -> playingPositionsTmdbMovie.put(key, progress)
            is MovieKey.Hosted -> playingPositionsHostedMovie.put(key, progress)
        }
    }

    override suspend fun removeLastPlayedPosition(key: StreamableKey) {
        when (key) {
            is EpisodeKey.Tmdb -> playingPositionsTmdbTvShow.put(key.itemKey, null)
            is EpisodeKey.Hosted -> playingPositionsHostedTvShow.put(key.itemKey, null)
            is MovieKey.Tmdb -> playingPositionsTmdbMovie.put(key, null)
            is MovieKey.Hosted -> playingPositionsHostedMovie.put(key, null)
        }
    }
}