package com.tajmoti.libtmdb

import com.tajmoti.libtmdb.model.find.FindResponse
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.rektor.Rektor
import com.tajmoti.rektor.Template
import com.tajmoti.rektor.params

class RektorTmdbService(private val rektor: Rektor) : TmdbService {
    private val find = Template.get<FindResponse>("3/find/{external_id}")
    private val searchTv = Template.get<SearchTvResponse>("3/search/tv")
    private val searchMovie = Template.get<SearchMovieResponse>("3/search/movie")
    private val getTv = Template.get<Tv>("3/tv/{tv_id}")
    private val getSeason = Template.get<Season>("3/tv/{tv_id}/season/{season_number}")
    private val getEpisode = Template.get<Episode>("3/tv/{tv_id}/season/{season_number}/episode/{episode_number}")
    private val getMovie = Template.get<Movie>("3//movie/{movie_id}")


    override suspend fun find(externalId: String, language: String, externalSource: String): FindResponse {
        return rektor.execute(
            find,
            placeholders = mapOf(
                "external_id" to externalId,
            ),
            queryParams = mapOf(
                "language" to language,
                "externalSource" to externalSource,
            )
        )
    }

    override suspend fun searchTv(query: String, firstAirDateYear: Int?): SearchTvResponse {
        return rektor.execute(
            searchTv,
            queryParams = params(
                "query" to query,
                "firstAirDateYear" to firstAirDateYear,
            )
        )
    }

    override suspend fun searchMovie(query: String, firstAirDateYear: Int?): SearchMovieResponse {
        return rektor.execute(
            searchMovie,
            queryParams = params(
                "query" to query,
                "firstAirDateYear" to firstAirDateYear,
            )
        )
    }

    override suspend fun getTv(tvId: Long): Tv {
        return rektor.execute(
            getTv,
            placeholders = params(
                "tv_id" to tvId,
            )
        )
    }

    override suspend fun getSeason(tvId: Long, seasonNumber: Int): Season {
        return rektor.execute(
            getSeason,
            placeholders = params(
                "tv_id" to tvId,
                "season_number" to seasonNumber,
            )
        )
    }

    override suspend fun getEpisode(tvId: Long, seasonNumber: Int, episodeNumber: Int): Episode {
        return rektor.execute(
            getEpisode,
            placeholders = params(
                "tv_id" to tvId,
                "season_number" to seasonNumber,
                "episode_number" to episodeNumber,
            )
        )
    }

    override suspend fun getMovie(movieId: Long): Movie {
        return rektor.execute(
            getMovie,
            placeholders = params(
                "movie_id" to movieId
            )
        )
    }
}