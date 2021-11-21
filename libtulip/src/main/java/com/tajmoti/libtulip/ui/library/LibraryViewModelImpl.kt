package com.tajmoti.libtulip.ui.library

import com.tajmoti.commonutils.combine
import com.tajmoti.commonutils.parallelMapBoth
import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.misc.job.NetworkResult
import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.info.TulipItem
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.repository.FavoritesRepository
import com.tajmoti.libtulip.repository.PlayingHistoryRepository
import com.tajmoti.libtulip.repository.TmdbTvDataRepository
import com.tajmoti.libtulip.repository.getItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class LibraryViewModelImpl constructor(
    favoritesRepo: FavoritesRepository,
    private val historyRepository: PlayingHistoryRepository,
    private val hostedRepo: HostedInfoDataSource,
    private val tmdbRepo: TmdbTvDataRepository,
    viewModelScope: CoroutineScope
) : LibraryViewModel {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val favoriteItems = favoritesRepo.getUserFavorites()
        .flatMapLatest { mapFavorites(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun mapFavorites(favorites: List<ItemKey>): Flow<List<LibraryItem>> {
        val tmdbFavorites = favorites.filterIsInstance(ItemKey.Tmdb::class.java)
        val hostedFavorites = favorites.filterIsInstance(ItemKey.Hosted::class.java)
        val a = getTmdbFavorites(tmdbFavorites)
        val b = flow { emit(getHostedFavorites(hostedFavorites)) }
        return merge(a, b).onEmpty { emit(emptyList()) }
    }

    private suspend inline fun getHostedFavorites(items: List<ItemKey.Hosted>): List<LibraryItem> {
        return items
            .parallelMapBoth { hostedRepo.getItemByKey(it) }
            .mapNotNull { (key, item) ->
                val lastPlayedPosition = historyRepository.getLastPlayedPosition(key).firstOrNull()
                item?.let { LibraryItem(key, item.name ?: "", null, lastPlayedPosition) }
            }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun getTmdbFavorites(keyFlow: List<ItemKey.Tmdb>): Flow<List<LibraryItem>> {
        return keyFlow
            .map { tmdbRepo.getItem(it).map { a -> it to a } }
            .combine()
            .flatMapLatest { keyToResultList ->
                keyToResultList.map { (key, result) ->
                    historyRepository.getLastPlayedPosition(key)
                        .mapNotNull { netResultToLibraryItem(key, result, it) }
                }.combine()
            }
    }

    private fun netResultToLibraryItem(
        key: ItemKey.Tmdb,
        result: NetworkResult<out TulipItem.Tmdb>,
        position: LastPlayedPosition?
    ): LibraryItem? {
        return result.data?.let {
            val posterPath = "https://image.tmdb.org/t/p/original" + it.posterPath
            LibraryItem(key, it.name, posterPath, position)
        }
    }
}