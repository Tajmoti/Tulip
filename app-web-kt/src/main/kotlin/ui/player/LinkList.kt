package ui.player

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import react.Props
import react.dom.div
import react.fc

internal external interface VideoLinkListProps : Props {
    var links: List<StreamingSiteLinkDto>
    var current: StreamingSiteLinkDto?
    var onLinkClicked: (StreamingSiteLinkDto) -> Unit
}

internal val VideoLinkList = fc<VideoLinkListProps> { (links, current, onLinkClicked) ->
    div("list-group") {
        for (ref in links) {
            val active = current == ref
            VideoLink {
                attrs.link = ref
                attrs.active = active
                attrs.onLinkClicked = onLinkClicked
            }
        }
    }
}


private operator fun VideoLinkListProps.component1() = links
private operator fun VideoLinkListProps.component2() = current
private operator fun VideoLinkListProps.component3() = onLinkClicked
