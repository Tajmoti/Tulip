package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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

    override fun getLastPlayedPositionTmdb(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return flowOf(null)
    }

    override fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?> {
        return flowOf(null)
    }

    override fun getLastPlayedPositionHosted(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return flowOf(null)
    }

    override fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?> {
        return flowOf(null)
    }

    override suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float) {
    }

    override suspend fun removeLastPlayedPosition(key: StreamableKey) {
    }
}