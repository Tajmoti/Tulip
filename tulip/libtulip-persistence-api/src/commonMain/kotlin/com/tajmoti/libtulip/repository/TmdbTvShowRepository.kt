package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey

interface TmdbTvShowRepository : RwRepository<TvShow.Tmdb, TvShowKey.Tmdb>
