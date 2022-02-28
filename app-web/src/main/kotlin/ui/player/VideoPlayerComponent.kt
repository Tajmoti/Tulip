package ui.player

import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtvprovider.model.VideoStreamRef
import react.RBuilder
import react.dom.h1
import react.dom.iframe
import react.dom.video
import ui.react.ViewModelComponent
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
        val link = vmState.selectedLinkState.videoLinkToPlay
        val linkError = vmState.selectedLinkState.linkLoadingError
        val nonDirectLink = vmState.selectedLinkState.directLoadingUnsupported
        if (vmState.linkListState.linksLoading) {
            renderLoading("mb-5 text-info")
        } else if (vmState.selectedLinkState.loadingStreamOrDirectLink) {
            renderLoading("mb-5 text-primary")
        } else if (link != null) {
            renderVideoPlayer(link)
        } else if (linkError != null) {
            renderEmbeddedVideoPlayer(linkError.stream)
        } else if (nonDirectLink != null) {
            renderEmbeddedVideoPlayer(nonDirectLink.stream)
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

    private fun RBuilder.renderEmbeddedVideoPlayer(link: VideoStreamRef) {
        iframe(classes = "w-100") {
            attrs.src = link.url
            attrs.height = "480px"
        }
    }

    private fun RBuilder.renderError() {
        h1 { +"Shit, error :/" }
    }
}
