package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey

interface HostedMovieRepository : RwRepository<TulipMovie.Hosted, MovieKey.Hosted>
