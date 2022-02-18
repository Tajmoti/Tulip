package ui

import AppDiHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import react.Props
import react.RComponent
import react.State

abstract class TulipReactComponent<P : Props, S : State> : RComponent<P, S> {
    constructor() : super()
    constructor(props: P) : super(props)

    protected val scope = CoroutineScope(Dispatchers.Default)
    protected val di = AppDiHolder.di

//    override fun componentWillUnmount() {
//        scope.cancel()
//        super.componentWillUnmount()
//    }

    protected fun updateState(block: S.() -> Unit) {
        setState({ block(it); it })
    }

    protected infix fun <T> Flow<T>.flowTo(consumer: (T) -> Unit) {
        scope.launch { collect(consumer) }
    }

    protected infix fun <T : Any> Flow<T?>.flowValTo(consumer: (T) -> Unit) {
        scope.launch { collect { it?.let(consumer) } }
    }
}