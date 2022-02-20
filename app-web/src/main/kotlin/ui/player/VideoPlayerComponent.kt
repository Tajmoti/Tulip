package ui.player

import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.libtulip.ui.player.linkLoadingError
import com.tajmoti.libtulip.ui.player.videoLinkToPlay
import com.tajmoti.libtulip.ui.streams.LoadedLink
import react.RBuilder
import react.dom.h1
import react.dom.video
import ui.ViewModelComponent
import ui.renderLoading

class VideoPlayerComponent(props: VideoPlayerProps) :
    ViewModelComponent<VideoPlayerProps, VideoPlayerViewModel.State, VideoPlayerViewModel>(props) {
    override fun getViewModel() = VideoPlayerViewModelImpl(
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

    override fun RBuilder.render() {
        val link = vmState.videoLinkToPlay
        if (vmState.linkLoadingError != null) {
            renderError()
        } else if (link != null) {
            renderVideoPlayer(link)
        } else {
            renderLoading("mb-5")
        }
        renderLinkList()
    }

    private fun RBuilder.renderLinkList() {
        child(LinkListComponent::class) {
            attrs.viewModel = viewModel
            attrs.itemCallback = { viewModel.onStreamClicked(it, false) }
        }
    }

    private fun RBuilder.renderVideoPlayer(link: LoadedLink) {
        video("w-100") {
            attrs.src = link.directLink
            attrs.controls = true
            attrs.autoPlay = true
        }
    }

    private fun RBuilder.renderError() {
        h1 { +"Shit, error :/" }
    }
}
