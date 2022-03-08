package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtvprovider.model.VideoStreamRef
import react.Props
import react.dom.div
import react.fc

internal external interface VideoLinkListProps : Props {
    var links: List<UnloadedVideoStreamRef>
    var current: VideoStreamRef?
    var onLinkClicked: (UnloadedVideoStreamRef) -> Unit
}

internal val VideoLinkList = fc<VideoLinkListProps> { (links, current, onLinkClicked) ->
    div("list-group") {
        for (ref in links) {
            val active = current?.getInitiallySelectedLink() == ref.info
            VideoLink {
                attrs.link = ref
                attrs.active = active
                attrs.onLinkClicked = onLinkClicked
            }
        }
    }
}

private fun VideoStreamRef.getInitiallySelectedLink(): VideoStreamRef {
    return when (this) {
        is VideoStreamRef.Unresolved -> this
        is VideoStreamRef.Resolved -> origin ?: this
    }
}


private operator fun VideoLinkListProps.component1() = links
private operator fun VideoLinkListProps.component2() = current
private operator fun VideoLinkListProps.component3() = onLinkClicked
