package ui.player

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import react.Props
import react.dom.onClick
import react.fc
import ui.shared.ListButton

internal external interface VideoLinkProps : Props {
    var link: StreamingSiteLinkDto
    var active: Boolean?
    var onLinkClicked: (StreamingSiteLinkDto) -> Unit
}

internal val VideoLink = fc<VideoLinkProps> { (link, active, onLinkClicked) ->
    ListButton {
        attrs.active = active
        attrs.contents = {
            with(it) {
                attrs.onClick = { onLinkClicked(link) }
                LinkContents { attrs.link = link }
            }
        }
    }
}


private operator fun VideoLinkProps.component1() = link
private operator fun VideoLinkProps.component2() = active
private operator fun VideoLinkProps.component3() = onLinkClicked
