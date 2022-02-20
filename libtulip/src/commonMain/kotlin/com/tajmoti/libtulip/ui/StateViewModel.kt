package com.tajmoti.libtulip.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface StateViewModel<State : Any> {
    val state: StateFlow<State>
    val viewModelScope: CoroutineScope
}