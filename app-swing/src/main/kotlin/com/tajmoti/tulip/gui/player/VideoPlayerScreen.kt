package com.tajmoti.tulip.gui.player

import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.ui.player.PositionDelayingWrapper
import com.tajmoti.libtulip.ui.player.VideoPlayerUtils.streamableToDisplayName
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.tulip.gui.Screen
import com.tajmoti.tulip.gui.tvshow.ViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.util.concurrent.Executor
import javax.swing.JLayeredPane
import javax.swing.OverlayLayout
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

class VideoPlayerScreen(factory: ViewModelFactory, key: StreamableKey) : Screen<JLayeredPane>() {
    private val viewModel = factory.getPlayerViewModel(screenScope, key)

    /**
     * Executor that executes runnables on the Swing UI thread.
     */
    private val executor = Executor { SwingUtilities.invokeLater(it) }

    /**
     * VLC video player playing the media.
     */
    private lateinit var player: MediaPlayer

    /**
     * Overlay over the video with loading progress bar and information.
     */
    private lateinit var statusPanel: StatusPanel

    /**
     * VLC video player component
     */
    private lateinit var vlcComponent: EmbeddedMediaPlayerComponent

    /**
     * Name of the item being played.
     */
    val name = viewModel.streamableInfo.map { it?.let { streamableToDisplayName(it) } }

    /**
     * Whether the status panel should be displayed.
     */
    private val showStatusPanel = combine(
        viewModel.loadingStreamOrDirectLink,
        viewModel.linksLoading,
        viewModel.buffering.map { it?.let { it < 99.0f } ?: false }
    ) { a, b, c -> a or b or c }


    override val flowBindings = listOf(
        viewModel.videoLinkToPlay flowTo this::onVideoLinkToPlayChanged,
        showStatusPanel flowTo this::onLoadingChanged
    )


    private fun onLoadingChanged(isLoading: Boolean) {
        val front = if (isLoading) statusPanel.root else vlcComponent
        root.moveToFront(front)
    }

    override fun initialize(): JLayeredPane {
        val layer = JLayeredPane()
        layer.layout = OverlayLayout(layer)
        layer.add(StatusPanel(viewModel).also { statusPanel = it }.root, JLayeredPane.POPUP_LAYER)
        layer.add(setupVlcComponent().also { vlcComponent = it }, JLayeredPane.DEFAULT_LAYER)
        return layer
    }

    private fun setupVlcComponent(): EmbeddedMediaPlayerComponent {
        return EmbeddedMediaPlayerComponent()
            .apply { player = mediaPlayer() }
            .apply { mediaPlayer().submitOn { events().addMediaPlayerEventListener(LoggingPlayerEventListener()) } }
    }

    private fun onVideoLinkToPlayChanged(it: LoadedLink?) {
        if (it != null) {
            val vlcPlayer = DesktopVlcVideoPlayer(player, it.directLink, executor)
            val positionWorkaround = PositionDelayingWrapper(vlcPlayer, screenScope)
            viewModel.onMediaAttached(positionWorkaround)
        } else {
            viewModel.onMediaDetached()
        }
    }

    override fun cleanup() {
        super.cleanup()
        statusPanel.cleanup()
        thread(block = this::releaseVideoPlayer)
    }

    private fun releaseVideoPlayer() {
        logger.debug("Releasing player for ${viewModel.streamableKey.value}")
        player.submitOn {
            controls().stop()
            release()
            logger.debug("Player released for ${viewModel.streamableKey.value}")
        }
    }
}