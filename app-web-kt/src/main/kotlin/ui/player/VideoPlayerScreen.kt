package ui.player

import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import react.*
import ui.shared.LoadingSpinner
import ui.shared.SpinnerColor
import ui.useViewModel

external interface VideoPlayerScreenProps : Props {
    var streamableKey: StreamableKey
}

val VideoPlayerScreen = fc<VideoPlayerScreenProps> { (key) ->
    val (vm, state) = useViewModel<VideoPlayerViewModel, VideoPlayerViewModel.State>("", key)
    val (playerState, _) = useState(HtmlVideoPlayerState())
    useEffectOnce {
        vm.onMediaAttached(playerState)
        cleanup { vm.onMediaDetached() }
    }
    useEffect { vm.changeStreamable(key) }

    state.streamableInfo?.let { info ->
        VideoPlayerHeader {
            attrs.name = info
            attrs.data = state.tvShowData
        }
    }

    val link = state.selectedLinkState.videoLinkToPlay
    val linkError = state.selectedLinkState.linkLoadingError
    val nonDirectLink = state.selectedLinkState.directLoadingUnsupported
    if (state.linkListState.showNoLinksYetLoadingProgress) {
        LoadingSpinner { attrs.color = SpinnerColor.Info }
    } else if (state.selectedLinkState.showSelectedLinkLoadingProgress) {
        LoadingSpinner { attrs.color = SpinnerColor.Primary }
    } else if (link != null) {
        HtmlVideoPlayer {
            attrs.link = link
            attrs.onStateChanged = { playerState.updateState(it) }
            attrs.initialProgress = playerState.initialProgress
        }
    } else if (linkError != null) {
        IframeVideoPlayer { attrs.link = linkError.stream }
    } else if (nonDirectLink != null) {
        IframeVideoPlayer { attrs.link = nonDirectLink.stream }
    }
    VideoLinkList {
        attrs.links = vm.linksResult.value ?: emptyList()
        attrs.current = vm.videoLinkPreparingOrPlaying.value
        attrs.onLinkClicked = { vm.onStreamClicked(it, false) }
    }
    if (state.linkListState.showLinksStillLoadingProgress) {
        LinkLoadingProgressBar {}
    }
}


private operator fun VideoPlayerScreenProps.component1() = streamableKey
