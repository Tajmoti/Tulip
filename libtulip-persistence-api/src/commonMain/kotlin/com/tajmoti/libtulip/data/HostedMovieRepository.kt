package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey

interface HostedMovieRepository : RwRepository<TulipMovie.Hosted, MovieKey.Hosted>
