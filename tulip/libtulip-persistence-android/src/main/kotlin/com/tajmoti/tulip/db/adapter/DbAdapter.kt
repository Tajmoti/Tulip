package com.tajmoti.tulip.db.adapter

import kotlinx.coroutines.flow.Flow

interface DbAdapter<DAO, ID, E> {
    fun findByKeyFromDb(dao: DAO, key: ID): Flow<E?>

    suspend fun insertToDb(dao: DAO, entity: E)

    suspend fun insertToDb(dao: DAO, entities: List<E>)
}