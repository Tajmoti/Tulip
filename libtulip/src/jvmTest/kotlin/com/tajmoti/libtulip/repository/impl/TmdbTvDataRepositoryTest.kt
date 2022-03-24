package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtmdb.LibTmdbRepository
import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.search.TvSearchResult
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.impl.InMemoryLocalTvDataSource
import com.tajmoti.libtulip.model.info.TulipMovie
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class TmdbTvDataRepositoryTest {
    private val zeroCacheParams = TulipConfiguration.CacheParameters(0, 0)
    private val activeCacheParams = TulipConfiguration.CacheParameters(60 * 1_000, 2048)

    private val tvInputs = listOf(
        1500 to TvSearchResult(id = 1, name = "A", posterPath = null, backdropPath = null),
        1500 to TvSearchResult(id = 2, name = "B", posterPath = null, backdropPath = null),
        1500 to TvSearchResult(id = 3, name = "C", posterPath = null, backdropPath = null),
        2000 to TvSearchResult(id = 4, name = "D", posterPath = null, backdropPath = null),
        2000 to TvSearchResult(id = 5, name = "E", posterPath = null, backdropPath = null),
        2000 to TvSearchResult(6, "F", "f", "ff"),
    )

    private val movieInputs = listOf(
        1500 to Movie(id = 7, title = "A", overview = "First", posterPath = "g", backdropPath = null),
        1500 to Movie(id = 8, title = "B", overview = "Second", posterPath = "h", backdropPath = null),
        1500 to Movie(id = 9, title = "C", overview = "Third", posterPath = "i", backdropPath = null),
        2000 to Movie(id = 10, title = "D", overview = "Fourth", posterPath = "j", backdropPath = null),
        2000 to Movie(id = 11, title = "E", overview = "Fifth", posterPath = "k", backdropPath = null),
        2000 to Movie(id = 12, title = "F", overview = "Sixth", posterPath = "l", backdropPath = null),
    )

    @Test
    fun `findTvShowKey finds correct TV show`() = runBlocking {
        val repo = createRepo(tmdbService = createTmdbServiceForTvSearch())
        for ((year, tv) in tvInputs) {
            assertEquals(TvShowKey.Tmdb(tv.id), repo.findTvShowKey(tv.name, year).first().data)
        }
    }

    @Test
    fun `findMovieKey finds correct movie`() = runBlocking {
        val repo = createRepo(tmdbService = createTmdbServiceForMovie())
        for ((year, movie) in movieInputs) {
            val actual = repo.findMovieKey(movie.title, year).first().data
            assertEquals(MovieKey.Tmdb(movie.id), actual)
        }
    }

    @Test
    fun `findTvShowKey caches results across calls`() = runBlocking {
        val tmdbApiMock = createTmdbServiceForTvSearch()
        val repo = createRepo(cache = activeCacheParams, tmdbService = tmdbApiMock)
        for (i in 0 until 64) {
            for ((year, tv) in tvInputs) {
                repo.findTvShowKey(tv.name, year).take(1).collect { }
            }
        }
        for ((year, tv) in tvInputs) {
            verify(tmdbApiMock, times(1)).searchTv(tv.name, year)
        }
    }

    @Test
    fun `findMovieKey caches results across calls`() = runBlocking {
        val tmdbApiMock = createTmdbServiceForMovie()
        val repo = createRepo(cache = activeCacheParams, tmdbService = tmdbApiMock)
        for (i in 0 until 64) {
            for ((year, movie) in movieInputs) {
                repo.findMovieKey(movie.name, year).take(1).collect { }
            }
        }
        for ((year, movie) in movieInputs) {
            verify(tmdbApiMock, times(1)).searchMovie(movie.name, year)
        }
    }

    @Test
    fun `getMovie returns correct movie`() = runBlocking {
        val tmdbApiMock = createTmdbServiceForMovieGet()
        val repo = createRepo(tmdbService = tmdbApiMock, localDataSource = InMemoryLocalTvDataSource())
        for ((_, movie) in movieInputs) {
            val key = MovieKey.Tmdb(movie.id)
            val expect = TulipMovie.Tmdb(key, movie.name, movie.overview, movie.posterPath, movie.backdropPath)
            assertEquals(expect, repo.getMovie(key).first().data)
        }
    }

    private fun createRepo(
        cache: TulipConfiguration.CacheParameters = zeroCacheParams,
        tmdbService: TmdbService = mock(),
        localDataSource: LocalTvDataSource = mock()
    ): CachingTvDataRepository {
        return CachingTvDataRepository(LibTmdbRepository(tmdbService), localDataSource, cache)
    }

    private fun createTmdbServiceForTvSearch(): TmdbService = mock {
        for ((year, tv) in tvInputs) {
            onBlocking { searchTv(tv.name, year) } doReturn SearchTvResponse(listOf(tv))
        }
    }

    private fun createTmdbServiceForMovie(): TmdbService = mock {
        for ((year, movie) in movieInputs) {
            onBlocking { searchMovie(movie.title, year) } doReturn SearchMovieResponse(listOf(movie))
        }
    }

    private fun createTmdbServiceForMovieGet(): TmdbService = mock {
        for ((_, movie) in movieInputs) {
            onBlocking { getMovie(movie.id) } doReturn movie
        }
    }
}