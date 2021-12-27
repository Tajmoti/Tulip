@file:Suppress("UNUSED")

package com.tajmoti.commonutils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


suspend inline fun <A, B, C> Triple<Deferred<A>, Deferred<B>, Deferred<C>>.awaitAll(): Triple<A, B, C> {
    val (a, b, c) = awaitAll(first, second, third)
    @Suppress("UNCHECKED_CAST")
    return Triple(a as A, b as B, c as C)
}

suspend inline fun <A, B> Pair<Deferred<A>, Deferred<B>>.awaitAll(): Pair<A, B> {
    val (a, b) = awaitAll(first, second)
    @Suppress("UNCHECKED_CAST")
    return Pair(a as A, b as B)
}

suspend inline fun <R, S> List<R>.parallelMap(
    crossinline block: suspend CoroutineScope.(R) -> S
): List<S> {
    return coroutineScope {
        map { async { block.invoke(this, it) } }
    }.awaitAll()
}

suspend inline fun <R, S> List<R>.parallelMapToFlow(
    crossinline block: suspend CoroutineScope.(R) -> S
): Flow<S> {
    return channelFlow {
        coroutineScope {
            map {
                async {
                    send(block.invoke(this, it))
                }
            }
        }
    }
}

inline fun <P, R, S> Map<P, R>.parallelMapToFlow(
    crossinline block: suspend CoroutineScope.(P, R) -> S
): Flow<S> {
    return channelFlow {
        coroutineScope {
            map { (key, value) ->
                async {
                    send(block.invoke(this, key, value))
                }
            }
        }
    }
}

suspend inline fun <P, R, S> Map<P, R>.parallelMap(
    crossinline block: suspend CoroutineScope.(P, R) -> S
): List<S> {
    return mapToAsyncJobs(this, block)
}

suspend inline fun <R, S> List<R>.parallelMapBoth(
    crossinline block: suspend CoroutineScope.(R) -> S
): List<Pair<R, S>> {
    return coroutineScope {
        map { async { it to block.invoke(this, it) } }
    }.awaitAll()
}

suspend inline fun <R, S> List<R>.parallelMapBothReversed(
    crossinline block: suspend CoroutineScope.(R) -> S
): List<Pair<S, R>> {
    return coroutineScope {
        map { async { block.invoke(this, it) to it } }
    }.awaitAll()
}

suspend inline fun <A, B> mapToAsyncJobsPair(
    crossinline a: suspend () -> A,
    crossinline b: suspend () -> B,
): Pair<A, B> {
    return coroutineScope {
        val aDeferred = async { a() }
        val bDeferred = async { b() }
        Pair(aDeferred, bDeferred)
    }.awaitAll()
}

suspend inline fun <A, B, C> mapToAsyncJobsTriple(
    crossinline a: suspend () -> A,
    crossinline b: suspend () -> B,
    crossinline c: suspend () -> C,
): Triple<A, B, C> {
    return coroutineScope {
        val aDeferred = async { a() }
        val bDeferred = async { b() }
        val cDeferred = async { c() }
        Triple(aDeferred, bDeferred, cDeferred)
    }.awaitAll()
}

suspend inline fun <R, S, T> mapToAsyncJobs(
    items: Map<R, S>,
    crossinline block: suspend CoroutineScope.(R, S) -> T
): List<T> {
    return coroutineScope {
        items.map { async { block.invoke(this, it.key, it.value) } }
    }.awaitAll()
}

suspend fun mapToAsyncJobs(vararg tasks: suspend CoroutineScope.() -> Any) {
    coroutineScope {
        tasks.map { async { it(this) } }
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
 * Performs a running fold on the pairs, which get accumulated into a map.
 * The initial empty map is not emitted.
 */
fun <K, V> Flow<Pair<K, V>>.runningFoldConcatDropInitial(): Flow<Map<K, V>> {
    val initial = emptyMap<K, V>()
    return runningFold(initial) { a, b -> a + b }
        .filter { it.isNotEmpty() }
}

/**
 * Maps not-null values, nulls are passed on.
 */
inline fun <T, S> Flow<T?>.mapNotNulls(crossinline transform: suspend (T) -> S): Flow<S?> {
    return map {
        if (it != null) {
            transform(it)
        } else {
            null
        }
    }
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