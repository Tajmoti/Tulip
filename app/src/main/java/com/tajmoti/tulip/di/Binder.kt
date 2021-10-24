package com.tajmoti.tulip.di

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.di.Binder
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.tulip.datasource.AndroidHostedInfoDataSource
import com.tajmoti.tulip.datasource.AndroidTvDataSource
import com.tajmoti.tulip.datasource.AndroidUserDataDataSource
import com.tajmoti.tulip.service.impl.DownloadManagerVideoDownloadService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [Binder::class])
interface Binder {
    @Binds
    @Singleton
    fun provideVideoDownloader(s: DownloadManagerVideoDownloadService): VideoDownloadService

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