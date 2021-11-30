package com.tajmoti.libtulip.repository.impl

import com.tajmoti.libtmdb.TmdbService
import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.search.SearchMovieResponse
import com.tajmoti.libtmdb.model.search.SearchTvResponse
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.TulipConfiguration
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class StreamRepositoryImplTest {
    private val zeroCacheParams = TulipConfiguration.CacheParameters(0, 0)
    private val activeCacheParams = TulipConfiguration.CacheParameters(60 * 1_000, 2048)

    private val tvInputs = listOf(
        1500 to Tv(id = 1, name = "A", seasons = emptyList(), posterPath = null, backdropPath = null),
        1500 to Tv(id = 2, name = "B", seasons = emptyList(), posterPath = null, backdropPath = null),
        1500 to Tv(id = 3, name = "C", seasons = emptyList(), posterPath = null, backdropPath = null),
        2000 to Tv(id = 4, name = "D", seasons = emptyList(), posterPath = null, backdropPath = null),
        2000 to Tv(id = 5, name = "E", seasons = emptyList(), posterPath = null, backdropPath = null),
        2000 to Tv(id = 6, name = "F", seasons = emptyList(), posterPath = null, backdropPath = null),
    )

    private val movieInputs = listOf(
        1500 to Movie(id = 7, title = "A", overview = null, posterPath = null, backdropPath = null),
        1500 to Movie(id = 8, title = "B", overview = null, posterPath = null, backdropPath = null),
        1500 to Movie(id = 9, title = "C", overview = null, posterPath = null, backdropPath = null),
        2000 to Movie(id = 10, title = "D", overview = null, posterPath = null, backdropPath = null),
        2000 to Movie(id = 11, title = "E", overview = null, posterPath = null, backdropPath = null),
        2000 to Movie(id = 12, title = "F", overview = null, posterPath = null, backdropPath = null),
    )

    @Test
    fun `findTmdbIdTv finds correct ID`() = runBlocking {
        val repo = createRepo(tmdbService = createTmdbServiceForTv())
        for ((year, tv) in tvInputs) {
            assertEquals(TvShowKey.Tmdb(tv.id), repo.findTmdbIdTv(tv.name, year).first().data)
        }
    }

    @Test
    fun `findTmdbIdMovie finds correct ID`() = runBlocking {
        val repo = createRepo(tmdbService = createTmdbServiceForMovie())
        for ((year, movie) in movieInputs) {
            val actual = repo.findTmdbIdMovie(movie.title, year).first().data
            assertEquals(MovieKey.Tmdb(movie.id), actual)
        }
    }

    @Test
    fun `findTmdbIdTv caches results across calls`() = runBlocking {
        val tmdbApiMock = createTmdbServiceForTv()
        val repo = createRepo(cache = activeCacheParams, tmdbService = tmdbApiMock)
        for (i in 0 until 64) {
            for ((year, tv) in tvInputs) {
                repo.findTmdbIdTv(tv.name, year).take(1).collect { }
            }
        }
        for ((year, tv) in tvInputs) {
            verify(tmdbApiMock, times(1)).searchTv(tv.name, year)
        }
    }

    @Test
    fun `findTmdbIdMovie caches results across calls`() = runBlocking {
        val tmdbApiMock = createTmdbServiceForMovie()
        val repo = createRepo(cache = activeCacheParams, tmdbService = tmdbApiMock)
        for (i in 0 until 64) {
            for ((year, movie) in movieInputs) {
                repo.findTmdbIdMovie(movie.name, year).take(1).collect { }
            }
        }
        for ((year, movie) in movieInputs) {
            verify(tmdbApiMock, times(1)).searchMovie(movie.name, year)
        }
    }

    private fun createRepo(
        cache: TulipConfiguration.CacheParameters = zeroCacheParams,
        tmdbService: TmdbService = mock(),
        localDataSource: LocalTvDataSource = mock()
    ): TmdbTvDataRepositoryImpl {
        return TmdbTvDataRepositoryImpl(tmdbService, localDataSource, cache)
    }

    private fun createTmdbServiceForTv(): TmdbService = mock {
        for ((year, tv) in tvInputs) {
            onBlocking { searchTv(tv.name, year) } doReturn SearchTvResponse(listOf(tv))
        }
    }

    private fun createTmdbServiceForMovie(): TmdbService = mock {
        for ((year, movie) in movieInputs) {
            onBlocking { searchMovie(movie.title, year) } doReturn SearchMovieResponse(listOf(movie))
        }
    }
}