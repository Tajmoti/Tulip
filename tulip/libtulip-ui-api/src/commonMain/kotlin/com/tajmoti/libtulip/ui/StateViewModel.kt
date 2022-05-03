package com.tajmoti.libtulip.ui

import com.tajmoti.commonutils.LibraryDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface StateViewModel<State : Any> {
    val state: StateFlow<State>
    val viewModelScope: CoroutineScope

    fun <T> Flow<T>.stateInOffload(initialValue: T): StateFlow<T> {
        return flowOn(LibraryDispatchers.libraryContext)
            .stateIn(viewModelScope, SharingStarted.Lazily, initialValue)
    }
}