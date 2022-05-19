package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey

interface TmdbMovieRepository : RwRepository<TulipMovie.Tmdb, MovieKey.Tmdb>
