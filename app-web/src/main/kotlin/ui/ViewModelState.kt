package ui

import react.State

external interface ViewModelState<T> : State {
    var state: T
}