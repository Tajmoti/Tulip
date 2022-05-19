package com.tajmoti.libtulip.facade

import com.tajmoti.commonutils.combineNonEmpty
import com.tajmoti.commonutils.mapBoth
import com.tajmoti.libtulip.dto.LibraryItemDto
import com.tajmoti.libtulip.dto.LibraryItemPlayingProgressDto
import com.tajmoti.libtulip.model.LastPlayedPosition
import com.tajmoti.libtulip.model.TulipItem
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.result.NetFlow
import com.tajmoti.libtulip.model.result.NetworkResult
import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.service.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class UserFavoriteFacadeImpl(
    private val favoritesRepository: UserFavoriteRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val historyRepository: PlayingHistoryRepository,
) : UserFavoriteFacade {

    override fun getUserFavorites(): Flow<List<LibraryItemDto>> {
        return favoritesRepository.getUserFavorites()
            .flatMapLatest { mapFavorites(it) }
            .mapNotNull { it }
    }

    override suspend fun addItemToFavorites(key: ItemKey) {
        favoritesRepository.addUserFavorite(key)
    }

    override suspend fun removeItemFromFavorites(key: ItemKey) {
        favoritesRepository.deleteUserFavorite(key)
    }

    private fun mapFavorites(favorites: Set<ItemKey>): Flow<List<LibraryItemDto>?> {
        val tmdbFavorites = favorites.filterIsInstance<ItemKey.Tmdb>()
        val hostedFavorites = favorites.filterIsInstance<ItemKey.Hosted>()
        val a = getTmdbFavorites(tmdbFavorites)
        val b = getHostedFavorites(hostedFavorites)
        return merge<List<LibraryItemDto>?>(a, b).onEmpty { emit(null) }
    }

    private fun getTmdbFavorites(keys: List<ItemKey.Tmdb>): Flow<List<LibraryItemDto>> {
        return mapKeysToLibraryItems(keys, tmdbRepo::getItem, ::createTmdbLibraryItem)
    }

    private fun getHostedFavorites(keys: List<ItemKey.Hosted>): Flow<List<LibraryItemDto>> {
        return mapKeysToLibraryItems(keys, hostedTvDataRepository::getItemByKey, ::createHostedLibraryItem)
    }

    private fun <K : ItemKey, I : TulipItem> mapKeysToLibraryItems(
        keys: List<K>,
        itemGetter: (K) -> NetFlow<out I>,
        libraryItemDtoCreator: (K, I, LastPlayedPosition?) -> LibraryItemDto
    ): Flow<List<LibraryItemDto>> {
        return keys
            .map { key -> itemGetter(key).mapBoth(key) }
            .combineNonEmpty()
            .flatMapLatest { keysToResults -> mapKeyResultPairsToLibraryItems(keysToResults, libraryItemDtoCreator) }
    }

    private fun <I : TulipItem, K : ItemKey> mapKeyResultPairsToLibraryItems(
        keysToResults: List<Pair<K, NetworkResult<out I>>>,
        libraryItemDtoCreator: (K, I, LastPlayedPosition?) -> LibraryItemDto
    ): Flow<List<LibraryItemDto>> {
        return keysToResults
            .mapNotNull { (key, itemResult) -> itemResult.data?.let { item -> key to item } }
            .map { (key, data) ->
                historyRepository.getLastPlayedPosition(key)
                    .map { pos -> libraryItemDtoCreator(key, data, pos) }
            }
            .combineNonEmpty()
    }

    private fun createTmdbLibraryItem(
        key: ItemKey.Tmdb,
        item: TulipItem.Tmdb,
        pos: LastPlayedPosition?
    ) = LibraryItemDto(key, item.name, item.posterUrl, pos?.let { LibraryItemPlayingProgressDto(it.key, it.progress) })

    private fun createHostedLibraryItem(
        key: ItemKey.Hosted,
        item: TulipItem.Hosted,
        pos: LastPlayedPosition?
    ) = LibraryItemDto(key, item.name, null, pos?.let { LibraryItemPlayingProgressDto(it.key, it.progress) })

}