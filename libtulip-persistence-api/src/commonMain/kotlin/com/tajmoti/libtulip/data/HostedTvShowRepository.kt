package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.TvShowKey

interface HostedTvShowRepository : RwRepository<TvShow.Hosted, TvShowKey.Hosted>
