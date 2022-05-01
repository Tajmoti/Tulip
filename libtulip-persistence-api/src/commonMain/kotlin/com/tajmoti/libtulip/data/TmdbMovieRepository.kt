package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey

interface TmdbMovieRepository : RwRepository<TulipMovie.Tmdb, MovieKey.Tmdb>
