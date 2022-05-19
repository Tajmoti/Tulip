package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

class BrowserHostedTvShowRepository : HostedTvShowRepository {
    private val tvShows = BrowserStorage<TvShowKey.Hosted, TvShow.Hosted>()

    override fun findByKey(key: TvShowKey.Hosted): Flow<TvShow.Hosted?> {
        return tvShows.get(key)
    }

    override suspend fun insert(repo: TvShow.Hosted) {
        tvShows.put(repo.key, repo)
    }
}