package com.tajmoti.libtulip.ui

import kotlinx.coroutines.flow.StateFlow

interface StateViewModel<State : Any> {
    val state: StateFlow<State>
}