package com.tajmoti.libtulip.data

import kotlinx.coroutines.flow.Flow

interface RwRepository<V, K> {

    fun findByKey(key: K): Flow<V?>

    suspend fun insert(repo: V)
}