package com.tajmoti.libtulip.model.tmdb

import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv

data class TmdbCompleteTvShow(
    val tv: Tv,
    val seasons: List<Season>
)