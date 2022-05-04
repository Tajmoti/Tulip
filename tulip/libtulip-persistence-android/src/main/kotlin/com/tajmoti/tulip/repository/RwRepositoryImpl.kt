package com.tajmoti.tulip.repository

import com.tajmoti.commonutils.mapNotNulls
import com.tajmoti.libtulip.repository.RwRepository
import com.tajmoti.tulip.adapter.DbAdapter
import com.tajmoti.tulip.mapper.Mapper
import kotlinx.coroutines.flow.Flow

class RwRepositoryImpl<DAO, R, K, D>(
    private val dao: DAO,
    val adapter: DbAdapter<DAO, K, D>,
    val mapper: Mapper<R, D>,
) : RwRepository<R, K> {

    override fun findByKey(key: K): Flow<R?> {
        return adapter.findByKeyFromDb(dao, key).mapNotNulls(mapper::fromDb)
    }

    override suspend fun insert(repo: R) {
        return adapter.insertToDb(dao, mapper.toDb(repo))
    }
}