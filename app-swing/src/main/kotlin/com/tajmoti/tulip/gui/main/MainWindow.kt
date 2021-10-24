package com.tajmoti.tulip.gui.main

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.gui.GuiConstants
import com.tajmoti.tulip.gui.Screen
import com.tajmoti.tulip.gui.addCloseButtonAt
import com.tajmoti.tulip.gui.library.LibraryScreen
import com.tajmoti.tulip.gui.player.VideoPlayerScreen
import com.tajmoti.tulip.gui.search.SearchScreen
import com.tajmoti.tulip.gui.tvshow.TvShowScreen
import com.tajmoti.tulip.gui.tvshow.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.awt.GridBagLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JTabbedPane
import javax.swing.WindowConstants

class MainWindow(
    private val viewModelFactory: ViewModelFactory,
) : Screen<JFrame>() {
    /**
     * Tabbed pane housing all the application content.
     */
    private lateinit var tabbedPane: JTabbedPane

    /**
     * Indices of TV show screens by their key.
     */
    private val tabIndexPerScreen = mutableMapOf<TvShowKey, Int>()


    override fun initialize(): JFrame {
        val container = JFrame("Tulip")
        container.layout = GridBagLayout()
        container.background = GuiConstants.COLOR_BACKGROUND
        container.pack()
        container.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        container.contentPane = createMainTabbedPane()
        return container
    }

    private fun createMainTabbedPane(): JTabbedPane {
        tabbedPane = JTabbedPane()
        val libraryScreen = LibraryScreen(viewModelFactory)
        val searchScreen = SearchScreen(viewModelFactory, this::onSearchItemSelected)
        tabbedPane.addTab("Library", GuiConstants.ICON_TV_SHOW, libraryScreen.root, "Library of favorite items")
        tabbedPane.addTab("Search", GuiConstants.ICON_SEARCH, searchScreen.root, "Search all available content")
        return tabbedPane
    }

    private fun onSearchItemSelected(it: ItemKey) {
        when (it) {
            is TvShowKey.Tmdb -> navigateToTvShow(it)
            is MovieKey.Tmdb -> goToMediaPlayback(it)
            else -> TODO()
        }
    }

    private fun goToMediaPlayback(key: StreamableKey) {
        createVideoPlayerTab(viewModelFactory, key)
    }

    private fun navigateToTvShow(key: TvShowKey) {
        val existingIndex = tabIndexPerScreen[key]
        if (existingIndex != null) {
            tabbedPane.selectedIndex = existingIndex
        } else {
            createTvShowTab(key)
        }
    }

    private fun createTvShowTab(key: TvShowKey) {
        val tvShowScreen = TvShowScreen(viewModelFactory, key)
        val (index, label) = addTabWithScreen(tvShowScreen) { closeTvShowTab(key) }
        tvShowScreen.screenScope
            .launch { tvShowScreen.name.filterNotNull().collect { name -> label.text = name } }
        tvShowScreen.screenScope
            .launch { tvShowScreen.episodeToPlay.filterNotNull().collect { episode -> goToMediaPlayback(episode) } }
        tabIndexPerScreen[key] = index
    }

    private fun closeTvShowTab(key: TvShowKey) {
        tabIndexPerScreen.remove(key)
    }

    private fun createVideoPlayerTab(viewModelFactory: ViewModelFactory, key: StreamableKey) {
        val screen = VideoPlayerScreen(viewModelFactory, key)
        val (_, label) = addTabWithScreen(screen) { }
        val streamableName = screen.name.filterNotNull()
        screen.screenScope.launch { streamableName.collect { name -> label.text = name } }
    }


    private fun addTabWithScreen(screen: Screen<*>, onClose: () -> Unit): Pair<Int, JLabel> {
        tabbedPane.addTab("Loading...", GuiConstants.ICON_TV_SHOW, screen.root)
        val newIndex = tabbedPane.tabCount - 1
        tabbedPane.selectedIndex = newIndex
        val label = tabbedPane.addCloseButtonAt(newIndex) { screen.cleanup(); onClose() }
        return Pair(newIndex, label)
    }
}