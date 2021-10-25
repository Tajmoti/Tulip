package com.tajmoti.libtulip.di

import dagger.Module

@Module(
    includes = [
        ApiServiceModule::class,
        BusinessLogicModule::class,
        DataRepositoryModule::class,
        NetworkingModule::class
    ]
)
interface TulipRootModule