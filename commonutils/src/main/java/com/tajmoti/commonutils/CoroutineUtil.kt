package com.tajmoti.commonutils

import kotlinx.coroutines.*


suspend inline fun <A, B, C> Triple<Deferred<A>, Deferred<B>, Deferred<C>>.awaitAll(): Triple<A, B, C> {
    val (a, b, c) = awaitAll(first, second, third)
    return Triple(a as A, b as B, c as C)
}

suspend inline fun <A, B> Pair<Deferred<A>, Deferred<B>>.awaitAll(): Pair<A, B> {
    val (a, b) = awaitAll(first, second)
    return Pair(a as A, b as B)
}

suspend inline fun <R, S> mapToAsyncJobs(
    items: List<R>,
    crossinline block: suspend CoroutineScope.(R) -> S
): List<S> {
    return coroutineScope {
        items.map { async { block.invoke(this, it) } }
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