package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.combine
import com.tajmoti.commonutils.parallelMapBoth
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
    viewModelScope: CoroutineScope,
) : LibraryViewModel {

    override val favoriteItems = favoritesRepo.getUserFavorites()
        .flatMapLatest { mapFavorites(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun mapFavorites(favorites: List<ItemKey>): Flow<List<LibraryItem>> {
        val tmdbFavorites = favorites.filterIsInstance(ItemKey.Tmdb::class.java)
        val hostedFavorites = favorites.filterIsInstance(ItemKey.Hosted::class.java)
        val a = getTmdbFavorites(tmdbFavorites)
        val b = flow { emit(getHostedFavorites(hostedFavorites)) }
        return merge(a, b).onEmpty { emit(emptyList()) }
    }

    private suspend inline fun getHostedFavorites(items: List<ItemKey.Hosted>): List<LibraryItem> {
        return items
            .parallelMapBoth { hostedTvDataRepository.getItemByKey(it).firstOrNull()?.toResult()?.getOrNull() }
            .mapNotNull { (key, item) ->
                val lastPlayedPosition = historyRepository.getLastPlayedPosition(key).firstOrNull()
                item?.let { LibraryItem(key, item.name ?: "", null, lastPlayedPosition) }
            }
    }

    private fun getTmdbFavorites(keyFlow: List<ItemKey.Tmdb>): Flow<List<LibraryItem>> {
        return keyFlow
            .map { key -> tmdbRepo.getItem(key).map { itemResult -> key to itemResult } }
            .combine()
            .flatMapLatest { keysToResults ->
                keysToResults.map { (key, result) ->
                    historyRepository.getLastPlayedPosition(key)
                        .mapNotNull { pos ->
                            result.data?.let { item -> LibraryItem(key, item.name, item.posterUrl, pos) }
                        }
                }.combine()
            }
    }
}