package ui.player

import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import react.Props
import react.fc
import ui.react.useViewModel
import ui.shared.LoadingSpinner
import ui.shared.SpinnerColor

external interface VideoPlayerScreenProps : Props {
    var streamableKey: StreamableKey
}

val VideoPlayerScreen = fc<VideoPlayerScreenProps> { (key) ->
    val (vm, state) = useViewModel<VideoPlayerViewModel, VideoPlayerViewModel.State>("", key) ?: return@fc

    val link = state.selectedLinkState.videoLinkToPlay
    val linkError = state.selectedLinkState.linkLoadingError
    val nonDirectLink = state.selectedLinkState.directLoadingUnsupported
    if (state.linkListState.linksLoading) {
        LoadingSpinner { attrs.color = SpinnerColor.Info }
    } else if (state.selectedLinkState.loadingStreamOrDirectLink) {
        LoadingSpinner { attrs.color = SpinnerColor.Primary }
    } else if (link != null) {
        HtmlVideoPlayer { attrs.link = link }
    } else if (linkError != null) {
        IframeVideoPlayer { attrs.link = linkError.stream }
    } else if (nonDirectLink != null) {
        IframeVideoPlayer { attrs.link = nonDirectLink.stream }
    }
    LinkList {
        attrs.links = vm.linksResult.value ?: emptyList()
        attrs.current = vm.videoLinkPreparingOrPlaying.value
        attrs.onLinkClicked = { vm.onStreamClicked(it, false) }
    }
}


private operator fun VideoPlayerScreenProps.component1() = streamableKey
