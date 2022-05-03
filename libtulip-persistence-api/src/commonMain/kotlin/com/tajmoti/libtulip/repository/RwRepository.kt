package com.tajmoti.libtulip.repository

import kotlinx.coroutines.flow.Flow

interface RwRepository<V, K> {

    fun findByKey(key: K): Flow<V?>

    suspend fun insert(repo: V)
}