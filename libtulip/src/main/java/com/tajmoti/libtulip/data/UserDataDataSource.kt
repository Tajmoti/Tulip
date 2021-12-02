package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface UserDataDataSource {
    /**
     * Checks whether the [item] was added to the user's favorites.
     */
    fun isFavorite(item: ItemKey): Flow<Boolean>

    /**
     * Returns all user's favorites.
     */
    fun getUserFavorites(): Flow<List<ItemKey>>

    /**
     * Removes an [item] from the user's favorites.
     * Does nothing if the [item] isn't in their favorites.
     */
    suspend fun deleteUserFavorite(item: ItemKey)

    /**
     * Adds an [item] to the user's favorites.
     * Does nothing if the [item] is already in their favorites.
     */
    suspend fun addUserFavorite(item: ItemKey)


    /**
     * Retrieves the last played position of the TV show or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPosition(key: ItemKey) = when (key) {
        is ItemKey.Tmdb -> getLastPlayedPositionTmdb(key)
        is ItemKey.Hosted -> getLastPlayedPositionHosted(key)
    }

    /**
     * Retrieves the last played position of the TV show or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPositionTmdb(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?>

    /**
     * Retrieves the last played position of the TV show or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPositionHosted(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?>



    /**
     * Retrieves the last played position of the TV show episode or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPosition(key: StreamableKey) = when (key) {
        is EpisodeKey.Tmdb -> getLastPlayedPositionTmdb(key)
        is EpisodeKey.Hosted -> getLastPlayedPositionHosted(key)
        is MovieKey.Tmdb -> flowOf(null) // TODO
        is MovieKey.Hosted -> flowOf(null) // TODO
    }

    /**
     * Retrieves the last played position of the TV show episode or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPositionTmdb(key: StreamableKey.Tmdb): Flow<LastPlayedPosition.Tmdb?>

    /**
     * Retrieves the last played position of the TV show episode or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPositionHosted(key: StreamableKey.Hosted): Flow<LastPlayedPosition.Hosted?>


    /**
     * Sets the playing [progress] of the TV show episode or movie specified by [key].
     * The [progress] is a real number in the range 0.0 to 1.0.
     */
    suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float?)
}