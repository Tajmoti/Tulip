package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.parallelMapBoth
import com.tajmoti.libtmdb.model.FindResult
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.misc.NetworkResult
import com.tajmoti.libtulip.model.hosted.toItemKey
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class LibraryViewModelImpl constructor(
    favoritesRepo: FavoritesRepository,
    private val hostedRepo: HostedInfoDataSource,
    private val tmdbRepo: TmdbTvDataRepository,
    viewModelScope: CoroutineScope
) : LibraryViewModel {

    @OptIn(FlowPreview::class)
    override val favoriteItems = favoritesRepo.getUserFavoritesAsFlow()
        .flatMapMerge { mapFavorites(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun mapFavorites(favorites: List<ItemKey>): Flow<List<LibraryItem>> {
        val tmdbFavorites = favorites.filterIsInstance(ItemKey.Tmdb::class.java)
        val hostedFavorites = favorites.filterIsInstance(ItemKey.Hosted::class.java)
        val a = getTmdbFavorites(tmdbFavorites)
        val b = flow<List<LibraryItem>> { getHostedFavorites(hostedFavorites) }
        return merge(a, b)
    }

    private suspend inline fun getHostedFavorites(items: List<ItemKey.Hosted>): List<LibraryItem> {
        return items
            .parallelMapBoth { hostedRepo.getItemByKey(it) }
            .mapNotNull { (key, item) -> item?.let { LibraryItem(key, item.name, null) } }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun getTmdbFavorites(keyFlow: List<ItemKey.Tmdb>): Flow<List<LibraryItem>> {
        val tmdbTvIds = keyFlow
            .map { it.id }
            .map { tmdbRepo.getItemAsFlow(it).map { a -> it to a } }
        return combine(*(tmdbTvIds).toTypedArray()) { it.toList() }
            .map { it.mapNotNull { (id, result) -> netResultToLibraryItem(id, result) } }
    }

    private fun netResultToLibraryItem(
        id: TmdbItemId,
        result: NetworkResult<out FindResult>
    ): LibraryItem? {
        return result.data?.let { mapTmdbItemToLibraryItem(id, it) }
    }

    private fun mapTmdbItemToLibraryItem(id: TmdbItemId, item: FindResult): LibraryItem {
        val posterPath = "https://image.tmdb.org/t/p/original" + item.posterPath
        return LibraryItem(id.toItemKey(), item.name, posterPath)
    }
}