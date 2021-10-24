package com.tajmoti.tulip.di

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.tulip.datasource.AndroidHostedInfoDataSource
import com.tajmoti.tulip.datasource.AndroidTvDataSource
import com.tajmoti.tulip.datasource.AndroidUserDataDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
interface AndroidDataStoreModule {

    @Binds
    @Singleton
    fun provideAndroidTvShowRepository(s: AndroidHostedInfoDataSource): HostedInfoDataSource

    @Binds
    @Singleton
    fun provideLocalTvDataSource(s: AndroidTvDataSource): LocalTvDataSource

    @Binds
    @Singleton
    fun provideUserFavoriteDataRepository(s: AndroidUserDataDataSource): UserDataDataSource
}