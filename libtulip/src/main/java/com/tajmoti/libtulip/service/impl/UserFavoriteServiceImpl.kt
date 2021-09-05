package com.tajmoti.libtulip.service.impl

import com.tajmoti.commonutils.parallelMap
import com.tajmoti.commonutils.parallelMapBoth
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.model.hosted.toItemKey
import com.tajmoti.libtulip.model.hosted.toKey
import com.tajmoti.libtulip.model.info.TulipItemInfo
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.model.userdata.UserFavorite
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.UserFavoritesService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserFavoriteServiceImpl @Inject constructor(
    private val repo: UserDataDataSource,
    private val tmdbRepo: TmdbTvDataRepository,
    private val hostedRepo: HostedTvDataRepository,
) : UserFavoritesService {

    override suspend fun getUserFavorites(): List<TulipItemInfo> {
        val favorites = repo.getUserFavorites()
        return mapFavorites(favorites)
    }

    private suspend fun mapFavorites(favorites: List<UserFavorite>): List<TulipItemInfo> {
        val tmdbShows = getTmdbFavorites(favorites)
        val hostedItems = getHostedFavorites(favorites)
        return tmdbShows + hostedItems
    }

    override fun getUserFavoritesAsFlow(): Flow<List<TulipItemInfo>> {
        return repo.getUserFavoritesAsFlow()
            .map { mapFavorites(it) }
    }

    private suspend fun getHostedFavorites(favorites: List<UserFavorite>): List<TulipItemInfo> {
        val hostedKeys = favorites.map { it.info }.filterIsInstance(ItemKey.Hosted::class.java)
        val pairs = hostedKeys.parallelMapBoth {
            hostedRepo.getItemByKey(it)
        }
        return pairs.mapNotNull { (key, item) ->
            item ?: return@mapNotNull null
            TulipItemInfo(key, item.name, null)
        }
    }

    private suspend fun getTmdbFavorites(favorites: List<UserFavorite>): List<TulipItemInfo> {
        val tmdb = favorites.map { it.info }.filterIsInstance(ItemKey.Tmdb::class.java)
        val tmdbTvIds = tmdb.map { it.id }.filterIsInstance(TmdbItemId.Tv::class.java)
        val tmdbMovieIds = tmdb.map { it.id }.filterIsInstance(TmdbItemId.Movie::class.java)
        val tmdbShows = tmdbTvIds.parallelMap {
            it to tmdbRepo.getTv(it.toKey())
        }
        val tmdbMovies = tmdbMovieIds.parallelMap {
            it to tmdbRepo.getMovie(it)
        }
        return (tmdbShows + tmdbMovies).mapNotNull { (id, item) ->
            item ?: return@mapNotNull null
            TulipItemInfo(
                id.toItemKey(),
                item.name,
                "https://image.tmdb.org/t/p/original" + item.posterPath
            )
        }
    }

    override suspend fun deleteUserFavorite(item: ItemKey) {
        repo.deleteUserFavorite(UserFavorite(item))
    }

    override suspend fun addUserFavorite(item: ItemKey) {
        repo.addUserFavorite(UserFavorite(item))
    }
}