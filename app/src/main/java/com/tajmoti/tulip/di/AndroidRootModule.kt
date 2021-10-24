package com.tajmoti.tulip.di

import com.tajmoti.libtulip.di.Binder
import com.tajmoti.libtulip.di.Provider
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(includes = [
    AndroidApiServiceModule::class,
    AndroidBusinessLogicModule::class,
    AndroidDataStoreModule::class,
    AndroidDataStoreProviderModule::class,
    AndroidNetworkModule::class,
    Binder::class,
    Provider::class
])
interface AndroidRootModule