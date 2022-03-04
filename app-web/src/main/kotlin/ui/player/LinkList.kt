package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtvprovider.model.VideoStreamRef
import react.Props
import react.dom.div
import react.fc

internal external interface LinkListProps : Props {
    var links: List<UnloadedVideoStreamRef>
    var current: VideoStreamRef?
    var onLinkClicked: (UnloadedVideoStreamRef) -> Unit
}

internal val LinkList = fc<LinkListProps> { (links, current, onLinkClicked) ->
    div("list-group") {
        for (ref in links) {
            val active = current?.getInitiallySelectedLink() == ref.info
            Link {
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


private operator fun LinkListProps.component1() = links
private operator fun LinkListProps.component2() = current
private operator fun LinkListProps.component3() = onLinkClicked
