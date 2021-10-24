package com.tajmoti.tulip.di

import com.tajmoti.libtulip.data.HostedInfoDataSource
import com.tajmoti.libtulip.data.LocalTvDataSource
import com.tajmoti.libtulip.data.UserDataDataSource
import com.tajmoti.libtulip.data.impl.InMemoryHostedInfoDataSource
import com.tajmoti.libtulip.data.impl.InMemoryLocalTvDataSource
import com.tajmoti.libtulip.data.impl.StubUserDataDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DesktopDataRepositoryModule {
    @Provides
    @Singleton
    fun provideAndroidTvShowRepository(): HostedInfoDataSource {
        return InMemoryHostedInfoDataSource()
    }

    @Provides
    @Singleton
    fun provideLocalTvDataSource(): LocalTvDataSource {
        return InMemoryLocalTvDataSource()
    }

    @Provides
    @Singleton
    fun provideUserFavoriteDataRepository(): UserDataDataSource {
        return StubUserDataDataSource()
    }
}