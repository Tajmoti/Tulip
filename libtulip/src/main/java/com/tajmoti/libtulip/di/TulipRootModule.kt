package com.tajmoti.libtulip.di

import dagger.Module

@Module(includes = [
    ApiServiceModule::class,
    Binder::class,
    Provider::class
])
interface TulipRootModule