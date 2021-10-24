package com.tajmoti.tulip

import com.formdev.flatlaf.FlatDarkLaf
import com.tajmoti.commonutils.logger
import com.tajmoti.tulip.di.DaggerDesktopAppComponent
import com.tajmoti.tulip.driver.SUPPORTED_DRIVERS
import com.tajmoti.tulip.gui.dependencies.DependenciesDialogFactory
import com.tajmoti.tulip.gui.main.MainWindow
import com.tajmoti.tulip.gui.tvshow.ViewModelFactory
import com.tajmoti.tulip.setup.DependencyManager
import jiconfont.icons.font_awesome.FontAwesome
import jiconfont.swing.IconFontSwing
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.awt.Dimension
import javax.swing.UIManager
import kotlin.math.roundToInt

class TulipApplication {

    fun start() {
        logger.info("Initializing app")
        setupLaF()
        loadBundledConfig()
        ensureWebDriversPresent()
        val viewModelFactory = DaggerDesktopAppComponent.create().getViewModelFactory()
        showMainWindow(viewModelFactory)
    }

    private fun loadBundledConfig() {
        logger.debug("Loading bundled configuration file")
        System.getProperties()
            .load(javaClass.classLoader.getResourceAsStream("configuration.properties"))
    }

    private fun ensureWebDriversPresent() {
        logger.debug("Preparing dependencies")
        val os = getOsInfo()
        val dependencyManager = DependencyManager(os, SUPPORTED_DRIVERS.mapNotNull { it.driverInfo })
        if (dependencyManager.allDependenciesPresent(os)) {
            logger.info("All web drivers were found")
            return
        }
        logger.info("Some dependencies are missing and will be downloaded")
        downloadWebDrivers(dependencyManager)
    }

    private fun downloadWebDrivers(dependencyManager: DependencyManager) {
        val steps = 100
        val (dialog, progressBar) = DependenciesDialogFactory.showDownloadingDialog(steps)
        runBlocking {
            val flow = dependencyManager.downloadMissingDependencies()
            flow.collect { progressBar.value = (steps * it).roundToInt() }
        }
        dialog.dispose()
    }

    private fun setupLaF() {
        IconFontSwing.register(FontAwesome.getIconFont())
        try {
            UIManager.setLookAndFeel(FlatDarkLaf())
        } catch (ex: Exception) {
            logger.warn("Failed to initialize LaF", ex)
        }
    }

    private fun showMainWindow(viewModelFactory: ViewModelFactory) {
        val window = MainWindow(viewModelFactory)
        window.root.setLocationRelativeTo(null)
        window.root.minimumSize = Dimension(640, 480)
        window.root.isVisible = true
    }
}

fun main() {
    val app = TulipApplication()
    app.start()
}