package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.db.dao.userdata.FavoritesDao
import com.tajmoti.tulip.db.dao.userdata.PlayingHistoryDao
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class AndroidUserDataDataSource @Inject constructor(
    private val userDataDao: FavoritesDao,
    private val playingHistoryDao: PlayingHistoryDao
) : UserDataDataSource {

    override fun isFavorite(item: ItemKey): Flow<Boolean> {
        return when (item) {
            is ItemKey.Tmdb -> item.toDb()
                .let { userDataDao.isTmdbFavorite(it.type, it.tmdbItemId) }
            is ItemKey.Hosted -> item.toDb()
                .let { userDataDao.isHostedFavorite(it.type, it.streamingService, it.key) }
            else -> emptyFlow()
        }
    }

    override fun getUserFavorites(): Flow<Set<ItemKey>> {
        val tmdbItems = userDataDao.getAllTmdbFavorites()
            .map { it.map { item -> item.fromDb() } }
        val hostedItems = userDataDao.getAllHostedFavorites()
            .map { it.map { item -> item.fromDb() } }
        return combine(tmdbItems, hostedItems) { a, b -> (a + b).toSet() }
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> userDataDao.deleteTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> userDataDao.deleteHostedFavorite(item.toDb())
        }
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        when (item) {
            is ItemKey.Tmdb -> userDataDao.insertTmdbFavorite(item.toDb())
            is ItemKey.Hosted -> userDataDao.insertHostedFavorite(item.toDb())
        }
    }

    override fun getLastPlayedPositionTmdb(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is TvShowKey.Tmdb -> playingHistoryDao.getLastPlayingPositionTvShowTmdb(key.id)
                .map { it?.fromDb() }
            is MovieKey.Tmdb -> playingHistoryDao.getLastPlayingPositionMovieTmdb(key.id)
                .map { it?.fromDb() }
        }
    }

    override fun getLastPlayedPositionHosted(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
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
                key.seasonNumber,
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
                playingHistoryDao.deleteLastPlayingPositionEpisodeTmdb(key.tvShowKey.id, key.seasonNumber, key.episodeNumber)
            is EpisodeKey.Hosted ->
                playingHistoryDao.deleteLastPlayingPositionEpisodeHosted(key.streamingService, key.tvShowKey.id, key.seasonNumber, key.id)
            is MovieKey.Hosted ->
                playingHistoryDao.deleteLastPlayingPositionMovieHosted(key.streamingService, key.id)
            is MovieKey.Tmdb ->
                playingHistoryDao.deleteLastPlayingPositionMovieTmdb(key.id)
        }
    }
}