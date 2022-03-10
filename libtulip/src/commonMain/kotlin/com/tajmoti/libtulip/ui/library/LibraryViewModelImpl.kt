package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.map
import com.tajmoti.commonutils.mapBoth
import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.info.TulipItem
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelImpl constructor(
    favoritesRepo: FavoritesRepository,
    private val historyRepository: PlayingHistoryRepository,
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    override val viewModelScope: CoroutineScope,
) : LibraryViewModel {

    private val favoriteItemsImpl = favoritesRepo.getUserFavorites()
        .flatMapLatest { mapFavorites(it) }
        .stateInOffload(null)

    private fun mapFavorites(favorites: Set<ItemKey>): Flow<List<LibraryItem>?> {
        val tmdbFavorites = favorites.filterIsInstance<ItemKey.Tmdb>()
        val hostedFavorites = favorites.filterIsInstance<ItemKey.Hosted>()
        val a = getTmdbFavorites(tmdbFavorites)
        val b = getHostedFavorites(hostedFavorites)
        return merge<List<LibraryItem>?>(a, b).onEmpty { emit(null) }
    }

    private fun getTmdbFavorites(keys: List<ItemKey.Tmdb>): Flow<List<LibraryItem>> {
        return mapKeysToLibraryItems(keys, tmdbRepo::getItem, ::createTmdbLibraryItem)
    }

    private fun getHostedFavorites(keys: List<ItemKey.Hosted>): Flow<List<LibraryItem>> {
        return mapKeysToLibraryItems(keys, hostedTvDataRepository::getItemByKey, ::createHostedLibraryItem)
    }

    private fun <K : ItemKey, I : TulipItem> mapKeysToLibraryItems(
        keys: List<K>,
        itemGetter: (K) -> NetFlow<out I>,
        libraryItemCreator: (K, I, LastPlayedPosition?) -> LibraryItem
    ): Flow<List<LibraryItem>> {
        return keys
            .map { key -> itemGetter(key).mapBoth(key) }
            .combineNonEmpty()
            .flatMapLatest { keysToResults -> mapKeyResultPairsToLibraryItems(keysToResults, libraryItemCreator) }
    }

    private fun <I : TulipItem, K : ItemKey> mapKeyResultPairsToLibraryItems(
        keysToResults: List<Pair<K, NetworkResult<out I>>>,
        libraryItemCreator: (K, I, LastPlayedPosition?) -> LibraryItem
    ): Flow<List<LibraryItem>> {
        return keysToResults
            .mapNotNull { (key, itemResult) -> itemResult.data?.let { item -> key to item } }
            .map { (key, data) ->
                historyRepository.getLastPlayedPosition(key)
                    .map { pos -> libraryItemCreator(key, data, pos) }
            }
            .combineNonEmpty()
    }

    private fun createTmdbLibraryItem(
        key: ItemKey.Tmdb,
        item: TulipItem.Tmdb,
        pos: LastPlayedPosition?
    ) = LibraryItem(key, item.name, item.posterUrl, pos)

    private fun createHostedLibraryItem(
        key: ItemKey.Hosted,
        item: TulipItem.Hosted,
        pos: LastPlayedPosition?
    ) = LibraryItem(key, item.name, null, pos)


    override val state = favoriteItemsImpl.map(viewModelScope) { LibraryViewModel.State(it) }
}