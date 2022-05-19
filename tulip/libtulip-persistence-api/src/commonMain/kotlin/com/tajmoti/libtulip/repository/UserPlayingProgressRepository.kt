package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.LastPlayedPosition
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import kotlinx.coroutines.flow.Flow

interface UserPlayingProgressRepository {

    /**
     * Retrieves the last played position of the TV show or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPosition(key: ItemKey) = when (key) {
        is ItemKey.Tmdb -> getLastPlayedPositionForTmdbItem(key)
        is ItemKey.Hosted -> getLastPlayedPositionForHostedItem(key)
    }

    /**
     * Retrieves the last played position of the TV show or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPositionForTmdbItem(key: ItemKey.Tmdb): Flow<LastPlayedPosition.Tmdb?>

    /**
     * Retrieves the last played position of the TV show or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPositionForHostedItem(key: ItemKey.Hosted): Flow<LastPlayedPosition.Hosted?>


    /**
     * Retrieves the last played position of the TV show episode or movie specified by [key].
     * The returned flow may never complete, and it may emit an updated value at any time!
     */
    fun getLastPlayedPosition(key: StreamableKey) = when (key) {
        is EpisodeKey.Tmdb -> getLastPlayedPositionTmdb(key)
        is EpisodeKey.Hosted -> getLastPlayedPositionHosted(key)
        is MovieKey.Tmdb -> getLastPlayedPositionTmdb(key as StreamableKey.Tmdb)
        is MovieKey.Hosted -> getLastPlayedPositionHosted(key as StreamableKey.Hosted)
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
    suspend fun setLastPlayedPosition(key: StreamableKey, progress: Float)

    /**
     * Removes the last playing progress for [key].
     */
    suspend fun removeLastPlayedPosition(key: StreamableKey)
}