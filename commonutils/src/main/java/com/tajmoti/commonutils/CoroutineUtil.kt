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

inline fun <T, R> StateFlow<T>.statefulMap(
    scope: CoroutineScope,
    crossinline transform: (value: T) -> R
): StateFlow<R> {
    val initial = transform(value)
    val flow = transform { value ->
        val transformed = transform(value)
        return@transform emit(transformed)
    }
    return flow.stateIn(scope, SharingStarted.Lazily, initial)
}