package com.tajmoti.tulip.di

import com.tajmoti.libtulip.di.TulipRootModule
import com.tajmoti.tulip.gui.tvshow.ViewModelFactory
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        TulipRootModule::class,
        DesktopBusinessLogicModule::class,
        DesktopConfigurationModule::class,
        DesktopDataRepositoryModule::class,
        DesktopViewModelModule::class,
        DesktopWebDriverModule::class
    ]
)
interface DesktopAppComponent {

    fun getViewModelFactory(): ViewModelFactory
}