package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.userdata.UserFavorite
import kotlinx.coroutines.flow.Flow

interface UserDataDataSource {

    suspend fun getUserFavorites(): List<UserFavorite>

    fun getUserFavoritesAsFlow(): Flow<List<UserFavorite>>

    suspend fun deleteUserFavorite(item: UserFavorite)

    suspend fun addUserFavorite(item: UserFavorite)
}