package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.StateFlow

interface TvShowViewModel {
    /**
     * Name of the TV show
     */
    val name: StateFlow<String?>

    /**
     * Data loading state
     */
    val state: StateFlow<State>

    /**
     * True if an error occurred during show loading
     */
    val error: StateFlow<Boolean>

    /**
     * True if this item is saved in the user's favorites
     */
    val isFavorite: StateFlow<Boolean>

    /**
     * Retries the last fetching request
     */
    fun retryFetchTvShowData()

    /**
     * Adds or removes this show from favorites
     */
    fun toggleFavorites(key: TvShowKey)

    sealed interface State {
        object Loading : State

        data class Success(
            val backdropPath: String?,
            val seasons: List<TulipSeasonInfo>
        ) : State

        object Error : State

        val success: Boolean
            get() = this is Success
    }
}