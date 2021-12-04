package com.tajmoti.tulip.di

import com.tajmoti.libtulip.di.TulipRootModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(includes = [
    TulipRootModule::class,
    AndroidBusinessLogicModule::class,
    AndroidConfigurationModule::class,
    AndroidDataStoreModule::class,
    AndroidDataStoreProviderModule::class,
    AndroidNetworkModule::class
])
interface AndroidRootModule