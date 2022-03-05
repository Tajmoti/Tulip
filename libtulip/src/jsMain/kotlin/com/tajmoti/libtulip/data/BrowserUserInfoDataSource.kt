package com.tajmoti.libtulip.data

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.HostedEpisodeProgress
import com.tajmoti.libtulip.model.TmdbEpisodeProgress
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BrowserUserInfoDataSource : UserDataDataSource {
    private val tmdbTvFavorites = BrowserStorage<Int, Set<TvShowKey.Tmdb>>("100")
    private val hostedTvFavorites = BrowserStorage<Int, Set<TvShowKey.Hosted>>("101")
    private val tmdbMovieFavorites = BrowserStorage<Int, Set<MovieKey.Tmdb>>("102")
    private val hostedMovieFavorites = BrowserStorage<Int, Set<MovieKey.Hosted>>("103")
    private val allFavorites = combine(
        tmdbTvFavorites.get(0),
        hostedTvFavorites.get(0),
        tmdbMovieFavorites.get(0),
        hostedMovieFavorites.get(0)
    ) { a, b, c, d -> a.orEmpty() + b.orEmpty() + c.orEmpty() + d.orEmpty() }
    private val playingPositionsTmdbTvShow = BrowserStorage<TvShowKey.Tmdb, TmdbEpisodeProgress>("200")
    private val playingPositionsTmdbMovie = BrowserStorage<MovieKey.Tmdb, Float>("201")
    private val playingPositionsHostedTvShow = BrowserStorage<TvShowKey.Hosted, HostedEpisodeProgress>("202")
    private val playingPositionsHostedMovie = BrowserStorage<MovieKey.Hosted, Float>("203")

    override fun isFavorite(item: ItemKey): Flow<Boolean> {
        return getUserFavorites().map { it.any { favorite -> favorite == item } }
    }

    override fun getUserFavorites(): Flow<Set<ItemKey>> {
        return allFavorites
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        when (item) {
            is MovieKey.Hosted -> minusItem(hostedMovieFavorites, item)
            is TvShowKey.Hosted -> minusItem(hostedTvFavorites, item)
            is MovieKey.Tmdb -> minusItem(tmdbMovieFavorites, item)
            is TvShowKey.Tmdb -> minusItem(tmdbTvFavorites, item)
        }
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        when (item) {
            is MovieKey.Hosted -> plusItem(hostedMovieFavorites, item)
            is TvShowKey.Hosted -> plusItem(hostedTvFavorites, item)
            is MovieKey.Tmdb -> plusItem(tmdbMovieFavorites, item)
            is TvShowKey.Tmdb -> plusItem(tmdbTvFavorites, item)
        }
    }

    private fun <T : ItemKey> plusItem(storage: BrowserStorage<Int, Set<T>>, item: T) {
        storage.update(0) { (it ?: emptySet()).plus(item) }
    }

    private fun <T : ItemKey> minusItem(storage: BrowserStorage<Int, Set<T>>, item: T) {
        storage.update(0) { (it ?: emptySet()).minus(item) }
    }

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