package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.SeasonKey
import kotlinx.coroutines.flow.StateFlow

interface TvShowViewModel {
    /**
     * Name of the TV show
     */
    val name: StateFlow<String?>

    /**
     * Backdrop image path of this TV show
     */
    val backdropPath: StateFlow<String?>

    /**
     * Seasons belonging to this TV show.
     * The list is sorted - specials come after all real seasons.
     */
    val seasons: StateFlow<List<TulipSeasonInfo>?>

    /**
     * Season to display episodes from.
     */
    val selectedSeason: StateFlow<SeasonKey?>

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
    fun toggleFavorites()

    /**
     * The user has selected a season.
     */
    fun onSeasonSelected(season: SeasonKey)
}