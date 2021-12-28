@file:Suppress("UNUSED")

package com.tajmoti.commonutils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


suspend inline fun <R, S> List<R>.parallelMap(
    crossinline block: suspend CoroutineScope.(R) -> S
): List<S> {
    return coroutineScope {
        map { async { block.invoke(this, it) } }
    }.awaitAll()
}

inline fun <T, R> StateFlow<T>.map(
    scope: CoroutineScope,
    crossinline transform: (value: T) -> R
): StateFlow<R> {
    val initial = transform(value)
    val flow = transform { value ->
        val transformed = transform(value)
        return@transform emit(transformed)
    }
    return flow.stateIn(scope, SharingStarted.Eagerly, initial)
}

fun <T, R> Flow<T>.mapBoth(other: R): Flow<Pair<R, T>> {
    return map { item -> other to item }
}

/**
 * Combines a list of flows into a flow that contains the joined lists.
 */
inline fun <reified T> List<Flow<T>>.combine(): Flow<List<T>> {
    return combine(*(this).toTypedArray()) { it.toList() }
}

/**
 * Combines a list of flows into a flow that contains the joined lists.
 * If the source list is empty, returns a flow of an empty list!
 */
inline fun <reified T> List<Flow<T>>.combineNonEmpty(): Flow<List<T>> {
    if (isEmpty()) return flowOf(emptyList())
    return combine()
}

/**
 * Performs a [combine] which behaves like [runningFold].
 * This means that the partial lists returned by each flow are added and then emitted in the resulting flow.
 * The difference from [runningFold] is that only the latest partial list is used every time.
 */
inline fun <reified T : Any> List<Flow<T>>.combineRunningFold(): Flow<List<T>> {
    return map { it.onStart<T?> { emit(null) } }
        .combine()
        .drop(1)
        .map { it.mapNotNull { value -> value } }
}


/**
 * Performs the [action] on each non-null item.
 */
inline fun <T> Flow<T?>.onEachNotNull(crossinline action: suspend (T) -> Unit): Flow<T?> {
    return onEach { if (it != null) action(it) }
}

/**
 * Performs the [action] on each null item.
 */
inline fun <T> Flow<T?>.onEachNull(crossinline action: suspend () -> Unit): Flow<T?> {
    return onEach { if (it == null) action() }
}

/**
 * Maps the items on the provided dispatcher.
 */
inline fun <T, R> Flow<T>.mapWithContext(
    dispatcher: CoroutineDispatcher,
    crossinline transform: suspend (value: T) -> R
): Flow<R> {
    return map {
        withContext(dispatcher) {
            transform(it)
        }
    }
}


/**
 * Maps not-null values, nulls are passed on.
 */
inline fun <T, S> Flow<T?>.mapNotNullsWithContext(
    dispatcher: CoroutineDispatcher,
    crossinline transform: suspend (T) -> S
): Flow<S?> {
    return map {
        if (it != null) {
            withContext(dispatcher) {
                transform(it)
            }
        } else {
            null
        }
    }
}

/**
 * Maps the items on the provided dispatcher.
 */
inline fun <T, R> Flow<T?>.mapFold(
    crossinline onValue: (T) -> R,
    crossinline onNull: () -> R
): Flow<R> {
    return map {
        if (it != null) {
            onValue(it)
        } else {
            onNull()
        }
    }
}

/**
 * Calls [action] with the last item that went through the flow.
 */
inline fun <T> Flow<T>.onLast(crossinline action: suspend FlowCollector<T>.(T?) -> Unit): Flow<T> {
    var previous: T? = null
    return onEach { previous = it }.onCompletion { action(previous) }
}