package com.tajmoti.libtulip.ui.tvshow

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.model.info.Season
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.ui.StateViewModel
import kotlinx.coroutines.flow.StateFlow

interface TvShowViewModel : StateViewModel<TvShowViewModel.State> {

    data class State(
        /**
         * Name of the TV show
         */
        val name: String?,
        /**
         * Backdrop image path of this TV show
         */
        val backdropPath: String?,
        /**
         * Seasons belonging to this TV show.
         * The list is sorted - specials come after all real seasons.
         */
        val seasons: List<Season>?,
        /**
         * Season to display episodes from.
         */
        val selectedSeason: SeasonWithEpisodes?,
        /**
         * Episode that isn't finished playing or null if none.
         */
        val lastPlayedEpisode: EpisodeKey?,
        /**
         * True if an error occurred during show loading
         */
        val error: Boolean,
        /**
         * True if this item is saved in the user's favorites
         */
        val isFavorite: Boolean,
    )

    /**
     * Name of the TV show
     */
    val name: StateFlow<String?>
        get() = state.map(viewModelScope, State::name)

    /**
     * Backdrop image path of this TV show
     */
    val backdropPath: StateFlow<String?>
        get() = state.map(viewModelScope, State::backdropPath)

    /**
     * Seasons belonging to this TV show.
     * The list is sorted - specials come after all real seasons.
     */
    val seasons: StateFlow<List<Season>?>
        get() = state.map(viewModelScope, State::seasons)

    /**
     * Season to display episodes from.
     */
    val selectedSeason: StateFlow<SeasonWithEpisodes?>
        get() = state.map(viewModelScope, State::selectedSeason)

    /**
     * True if an error occurred during show loading
     */
    val error: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::error)

    /**
     * True if this item is saved in the user's favorites
     */
    val isFavorite: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::isFavorite)

    /**
     * Adds or removes this show from favorites
     */
    fun toggleFavorites()

    /**
     * The user has selected a season.
     */
    fun onSeasonSelected(season: SeasonKey)
}