package com.tajmoti.tulip.gui.player

import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.tulip.gui.Screen
import kotlinx.coroutines.flow.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar

class StatusPanel(
    viewModel: VideoPlayerViewModel
) : Screen<JPanel>() {
    lateinit var label: JLabel
    lateinit var progress: JProgressBar

    /**
     * Whether the loading indicator should be displayed.
     */
    private val isLoading = combine(
        viewModel.loadingStreamOrDirectLink,
        viewModel.linksLoading,
        viewModel.buffering.map { it != null }
    ) { a, b, c -> a or b or c }

    /**
     * Status message for each of the possible states.
     */
    private val messageToDisplay = merge(
        viewModel.linksLoading.filter { it }.map { "Loading list of video links" },
        viewModel.isError.filter { it }.map { "Error occurred" },
        viewModel.linkLoadingError.map { "Link loading error occurred" },
        viewModel.linksNoResult.filter { it }.map { "No results" },
        viewModel.loadingStreamOrDirectLink.filter { it }.map { "Loading video" },
        viewModel.videoLinkToPlay.filter { it != null }.map { null },
        viewModel.buffering.filterNotNull().map { "Buffering ${it * 100}%" }
    )

    override val flowBindings = listOf(
        messageToDisplay flowTo this::onMessageToDisplayChanged,
        isLoading flowTo this::onLoadingChanged,
    )

    override fun initialize(): JPanel {
        val panel = JPanel()
        panel.layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.anchor = GridBagConstraints.CENTER
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0

        label = JLabel("", JLabel.CENTER)
        panel.add(label, gbc)

        gbc.gridy++
        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0

        progress = JProgressBar()
        progress.isIndeterminate = true
        panel.add(progress, gbc)
        return panel
    }

    private fun onMessageToDisplayChanged(it: String?) {
        root.isVisible = it != null
        label.text = it
    }

    private fun onLoadingChanged(it: Boolean) {
        progress.isVisible = it
    }
}