package com.tajmoti.libtulip.ui.search

import com.tajmoti.libtulip.model.search.TulipSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SearchViewModel {
    companion object {
        /**
         * How often a new search query will be started
         */
        const val DEBOUNCE_INTERVAL_MS = 500L
    }

    /**
     * True if currently searching a query
     */
    val loading: StateFlow<Boolean>

    /**
     * All search results for the entered query
     */
    val results: StateFlow<List<TulipSearchResult>>

    /**
     * Special UI state or null if search successful
     */
    val status: StateFlow<Icon?>

    /**
     * True if the retry button should be shown
     */
    val canTryAgain: StateFlow<Boolean>

    /**
     * Contains the item that should be opened
     */
    val itemToOpen: Flow<ItemKey>

    /**
     * Submit a new query to be searched
     */
    fun submitNewText(query: String)

    /**
     * Submits an already submitted text again
     */
    fun resubmitText()

    /**
     * The user has clicked an identified item
     */
    fun onItemClicked(id: TmdbItemId)

    enum class Icon {
        READY,
        NO_RESULTS,
        ERROR
    }
}