package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.Flow

class BrowserTmdbTvShowRepository : TmdbTvShowRepository {
    private val tvStorage = BrowserStorage<TvShowKey.Tmdb, TvShow.Tmdb>()

    override fun findByKey(key: TvShowKey.Tmdb): Flow<TvShow.Tmdb?> {
        return tvStorage.get(key)
    }

    override suspend fun insert(repo: TvShow.Tmdb) {
        tvStorage.put(repo.key, repo)
    }
}