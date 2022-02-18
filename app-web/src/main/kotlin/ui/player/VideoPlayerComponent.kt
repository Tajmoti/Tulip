package ui.player

import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.libtulip.ui.streams.LoadedLink
import react.RBuilder
import react.dom.h1
import react.dom.source
import react.dom.video
import ui.TulipReactComponent
import ui.renderLoading

class VideoPlayerComponent(props: VideoPlayerProps) : TulipReactComponent<VideoPlayerProps, VideoPlayerState>(props) {
    private val viewModel = VideoPlayerViewModelImpl(
        di.get(),
        di.get(),
        di.get(),
        di.get(),
        di.get(),
        di.get(),
        di.get(),
        di.get(),
        "",
        scope,
        props.streamableKey
    )

    init {
        state.status = VideoPlayerStatus.Loading
        viewModel.videoLinkToPlay flowValTo { updateState { status = VideoPlayerStatus.Loaded(it) } }
        viewModel.linkLoadingError flowTo { updateState { status = VideoPlayerStatus.Error } }
    }

    override fun RBuilder.render() {
        when (val state = state.status) {
            is VideoPlayerStatus.Loaded -> renderVideoPlayer(state.link)
            is VideoPlayerStatus.Loading -> renderLoading()
            is VideoPlayerStatus.Error -> renderError()
        }
    }

    private fun RBuilder.renderVideoPlayer(link: LoadedLink) {
        video("w-100") {
            attrs.controls = true
            attrs.autoPlay = true
            source {
                attrs.src = link.directLink
            }
        }
    }

    private fun RBuilder.renderError() {
        h1 { +"Shit, error :/" }
    }
}
