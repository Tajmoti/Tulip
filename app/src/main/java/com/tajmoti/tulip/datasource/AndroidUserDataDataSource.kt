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

    override fun getUserFavorites(): Flow<List<ItemKey>> {
        val tmdbItems = userDataDao.getAllTmdbFavorites()
            .map { it.map { item -> item.fromDb() } }
        val hostedItems = userDataDao.getAllHostedFavorites()
            .map { it.map { item -> item.fromDb() } }
        return combine(tmdbItems, hostedItems) { a, b -> a + b }
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

    override fun getLastPlayedPosition(key: ItemKey): Flow<LastPlayedPosition?> {
        return when (key) {
            is ItemKey.Tmdb -> getLastPlayedPositionTmdb(key)
            is ItemKey.Hosted -> getLastPlayedPositionHosted(key)
        }
    }

    override fun getLastPlayedPositionTmdb(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is TvShowKey.Tmdb -> playingHistoryDao.getLastPlayingPositionTmdb(
                key.id.id
            ).map { it?.fromDb() }
            is MovieKey.Tmdb -> flowOf(null) // TODO
        }
    }

    override fun getLastPlayedPositionHosted(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return when (key) {
            is TvShowKey.Hosted -> playingHistoryDao.getLastPlayingPositionHosted(
                key.streamingService,
                key.id
            ).map { it?.fromDb() }
            is MovieKey.Hosted -> flowOf(null) // TODO
        }
    }

    override fun getLastPlayedPosition(key: StreamableKey): Flow<LastPlayedPosition?> {
        return when (key) {
            is EpisodeKey.Tmdb -> getLastPlayedPositionTmdb(key)
            is EpisodeKey.Hosted -> getLastPlayedPositionHosted(key)
            is MovieKey.Tmdb -> flowOf(null) // TODO
            is MovieKey.Hosted -> flowOf(null) // TODO
        }
    }

    override fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return when (key) {
            is EpisodeKey.Tmdb -> playingHistoryDao.getLastPlayingPositionEpisodeTmdb(
                key.seasonKey.tvShowKey.id.id,
                key.seasonNumber,
                key.episodeNumber
            ).map { it?.fromDb() }
            is MovieKey.Tmdb -> flowOf(null) // TODO
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
            is MovieKey.Hosted -> flowOf(null) // TODO
        }
    }

    override suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float?) {
        return when (key) {
            is EpisodeKey.Tmdb ->
                playingHistoryDao.insertLastPlayingPositionTmdb(key.toLastPositionDb(progress))
            is EpisodeKey.Hosted ->
                playingHistoryDao.insertLastPlayingPositionHosted(key.toLastPositionDb(progress))
            is MovieKey.Hosted ->
                Unit // TODO
            is MovieKey.Tmdb ->
                Unit // TODO
        }
    }
}