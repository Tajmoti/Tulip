package com.tajmoti.libtulip.ui.search

import com.tajmoti.commonutils.map
import com.tajmoti.libtulip.dto.SearchResultDto
import com.tajmoti.libtulip.ui.StateViewModel
import kotlinx.coroutines.flow.StateFlow

interface SearchViewModel : StateViewModel<SearchViewModel.State> {
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
        get() = state.map(viewModelScope, State::loading)

    /**
     * All search results for the entered query.
     * Unrecognized results are moved to the end of the list.
     */
    val results: StateFlow<List<SearchResultDto>>
        get() = state.map(viewModelScope, State::results)

    /**
     * Special UI state or null if search successful
     */
    val status: StateFlow<Icon?>
        get() = state.map(viewModelScope, State::status)

    /**
     * True if the retry button should be shown
     */
    val canTryAgain: StateFlow<Boolean>
        get() = state.map(viewModelScope, State::canTryAgain)

    /**
     * Submit a new query to be searched
     */
    fun submitNewText(query: String)

    /**
     * Submits an already submitted text again
     */
    fun resubmitText()

    data class State(
        /**
         * True if currently searching a query
         */
        val loading: Boolean,
        /**
         * All search results for the entered query.
         * Unrecognized results are moved to the end of the list.
         */
        val results: List<SearchResultDto>,

        /**
         * Special UI state or null if search successful
         */
        val status: Icon?,

        /**
         * True if the retry button should be shown
         */
        val canTryAgain: Boolean,
    )

    enum class Icon {
        READY,
        NO_RESULTS,
        ERROR
    }
}