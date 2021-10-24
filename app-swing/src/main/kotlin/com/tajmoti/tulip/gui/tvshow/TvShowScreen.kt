package com.tajmoti.tulip.gui.tvshow

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.gui.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class TvShowScreen(viewModelFactory: ViewModelFactory, key: TvShowKey) : Screen<JScrollPane>() {
    private val viewModel = viewModelFactory.getTvShowViewModel(screenScope, key)

    private lateinit var tree: JTree
    private lateinit var scroll: JScrollPane

    /**
     * Name of the TV show. Loading takes a while.
     */
    val name = viewModel.name

    /**
     * Episode that the user has clicked and should be played.
     */
    val episodeToPlay = MutableStateFlow<EpisodeKey?>(null)


    override fun initialize(): JScrollPane {
        tree = JTree()
        scroll = JScrollPane(tree)
        return scroll
    }

    private val seasonsWithName = combine(viewModel.seasons, viewModel.name) { a, b -> a to b }

    override val flowBindings = listOf(
        seasonsWithName flowTo this::setSeasonsView,
    )

    private fun setSeasonsView(data: Pair<List<TulipSeasonInfo>?, String?>) {
        val (seasons, name) = data
        seasons ?: return
        name ?: return
        val top = DefaultMutableTreeNode(name)
        createNodes(top, seasons)
        tree.model = DefaultTreeModel(top)
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onMouseClickedOnTree(e)
            }
        })
        scroll.viewport.view = tree
    }

    private fun createNodes(root: DefaultMutableTreeNode, seasons: List<TulipSeasonInfo>) {
        for (season in seasons) {
            val seasonNode = DefaultMutableTreeNode(SeasonItem(season))
            root.add(seasonNode)
            for (episode in season.episodes) {
                val episodeNode = DefaultMutableTreeNode(EpisodeItem(episode))
                seasonNode.add(episodeNode)
            }
        }
    }

    private fun onMouseClickedOnTree(me: MouseEvent) {
        val tp = tree.getPathForLocation(me.x, me.y) ?: return
        val mutableTreeNode = (tp.lastPathComponent as DefaultMutableTreeNode)
        if (mutableTreeNode.depth != 0)
            return
        val episode = mutableTreeNode.userObject as EpisodeItem
        onEpisodeSelected(episode)
    }

    private fun onEpisodeSelected(episode: EpisodeItem) {
        episodeToPlay.value = episode.info.key
    }

    class SeasonItem(private val info: TulipSeasonInfo) {
        private fun seasonToStr(season: TulipSeasonInfo) =
            if (season.seasonNumber == 0) {
                "Specials"
            } else {
                "Season " + season.key.seasonNumber
            }

        override fun toString(): String {
            return seasonToStr(info)
        }
    }

    class EpisodeItem(val info: TulipEpisodeInfo) {
        private fun episodeToStr(episode: TulipEpisodeInfo) =
            "Episode " + episode.episodeNumber + ": " + episode.name

        override fun toString(): String {
            return episodeToStr(info)
        }
    }
}