package com.tajmoti.tulip.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tajmoti.commonutils.mapToAsyncJobsPair
import com.tajmoti.commonutils.parallelMap
import com.tajmoti.commonutils.parallelMapBoth
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.model.hosted.toItemKey
import com.tajmoti.libtulip.model.hosted.toKey
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    favoritesRepository: FavoritesRepository,
    private val hostedRepo: HostedInfoDataSource,
    private val tmdbRepo: TmdbTvDataRepository
) : ViewModel() {

    /**
     * All items that the user has marked as favorite
     */
    val favoriteItems = favoritesRepository.getUserFavoritesAsFlow().map { mapFavorites(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    private suspend inline fun mapFavorites(favorites: List<ItemKey>): List<LibraryItem> {
        val (tmdbShows, hostedItems) = mapToAsyncJobsPair(
            { getTmdbFavorites(favorites.filterIsInstance(ItemKey.Tmdb::class.java)) },
            { getHostedFavorites(favorites.filterIsInstance(ItemKey.Hosted::class.java)) }
        )
        return tmdbShows + hostedItems
    }

    private suspend inline fun getHostedFavorites(
        favorites: List<ItemKey.Hosted>
    ): List<LibraryItem> {
        val pairs = favorites
            .parallelMapBoth { hostedRepo.getItemByKey(it) }
        return pairs.mapNotNull { (key, item) ->
            item ?: return@mapNotNull null
            LibraryItem(key, item.name, null)
        }
    }

    private suspend inline fun getTmdbFavorites(
        favorites: List<ItemKey.Tmdb>
    ): List<LibraryItem> {
        val tmdbTvIds = favorites.map { it.id }
            .filterIsInstance(TmdbItemId.Tv::class.java)
        val tmdbMovieIds = favorites.map { it.id }
            .filterIsInstance(TmdbItemId.Movie::class.java)
        val (tmdbShows, tmdbMovies) = mapToAsyncJobsPair(
            { tmdbTvIds.parallelMap { it to tmdbRepo.getTv(it.toKey()) } },
            { tmdbMovieIds.parallelMap { it to tmdbRepo.getMovie(it) } }
        )
        return (tmdbShows + tmdbMovies).mapNotNull { (id, item) ->
            item ?: return@mapNotNull null
            val posterPath = "https://image.tmdb.org/t/p/original" + item.posterPath
            LibraryItem(id.toItemKey(), item.name, posterPath)
        }
    }
}