package com.tajmoti.tulip.gui.search

import com.tajmoti.libtulip.model.hosted.MappedSearchResult
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.tulip.gui.Screen
import com.tajmoti.tulip.gui.main.ReactiveListModel
import com.tajmoti.tulip.gui.tvshow.ViewModelFactory
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SearchScreen(
    viewModelFactory: ViewModelFactory,
    private val onItemDoubleClicked: (ItemKey) -> Unit,
) : Screen<Component>() {
    private val searchViewModel = viewModelFactory.getSearchViewModel(screenScope)
    private val resultListModel = ReactiveListModel<GroupedSearchResult>()


    override fun initialize(): Component {
        val panel = JPanel(GridBagLayout())

        val gbc = GridBagConstraints()
        gbc.gridy = 0
        gbc.gridx = 0
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL

        val field = HintTextField("Enter TV show or movie name...")
        field.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                val fullText = field.text + e.keyChar
                search(fullText)
            }
        })
        panel.add(field, gbc)

        val list = createResultsList()
        val resultList = JScrollPane(list)
        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                val index = list.locationToIndex(evt.point)
                val item = resultListModel.getElementAt(index)
                onItemClicked(item)
            }
        })
        gbc.gridy++
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(resultList, gbc)
        return panel
    }

    override val flowBindings = listOf(
        searchViewModel.results flowTo { resultListModel.items = it }
    )

    private fun onItemClicked(item: GroupedSearchResult) {
        when (item) {
            is GroupedSearchResult.TvShow -> onItemDoubleClicked(item.tmdbId)
            is GroupedSearchResult.Movie -> onItemDoubleClicked(item.tmdbId)
            is GroupedSearchResult.UnrecognizedTvShow -> showUnrecognizedResults(item.results)
            is GroupedSearchResult.UnrecognizedMovie -> showUnrecognizedResults(item.results)
        }
    }

    private fun createResultsList(): JList<GroupedSearchResult> {
        return JList(resultListModel)
            .apply { adjustCellRenderer(this, this@SearchScreen::resultToLabel) }
    }

    private fun showUnrecognizedResults(results: List<MappedSearchResult>) {
        TODO()
    }

    private fun resultToLabel(result: GroupedSearchResult): String {
        return when (result) {
            is GroupedSearchResult.TvShow -> result.results.first().info.name
            is GroupedSearchResult.Movie -> result.results.first().info.name
            is GroupedSearchResult.UnrecognizedTvShow -> "Other TV shows"
            is GroupedSearchResult.UnrecognizedMovie -> "Other movies"
        }
    }

    private fun <T> adjustCellRenderer(list: JList<T>, stringifier: (T) -> String) {
        val originalRenderer = list.cellRenderer
        list.cellRenderer = ListCellRenderer { a, item, c, d, e ->
            originalRenderer.getListCellRendererComponent(a, item, c, d, e)
                .also { (it as JLabel).text = stringifier(item) }
        }
    }

    private fun search(query: String) {
        searchViewModel.submitNewText(query)
    }
}