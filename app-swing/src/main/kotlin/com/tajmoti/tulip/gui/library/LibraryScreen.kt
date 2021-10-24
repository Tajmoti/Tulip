package com.tajmoti.tulip.gui.library

import com.tajmoti.libtulip.ui.library.LibraryItem
import com.tajmoti.tulip.gui.GuiConstants
import com.tajmoti.tulip.gui.Screen
import com.tajmoti.tulip.gui.main.ReactiveListModel
import com.tajmoti.tulip.gui.tvshow.ViewModelFactory
import javax.swing.JList

class LibraryScreen(viewModelFactory: ViewModelFactory) : Screen<JList<*>>() {
    /**
     * Library view
     */
    private val libraryViewModel = viewModelFactory.getLibraryViewModel(screenScope)
    private var libraryListModel = ReactiveListModel<LibraryItem>()

    override fun initialize(): JList<*> {
        val list = JList(libraryListModel)
        list.background = GuiConstants.COLOR_BACKGROUND
        return list
    }

    override val flowBindings = listOf(
        libraryViewModel.favoriteItems flowTo this::onFavoriteItemsChanged
    )

    private fun onFavoriteItemsChanged(it: List<LibraryItem>) {
        libraryListModel.items = it
    }
}