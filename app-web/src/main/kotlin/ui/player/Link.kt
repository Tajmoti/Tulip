package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import react.Props
import react.dom.onClick
import react.fc
import ui.shared.ListButton

internal external interface LinkProps : Props {
    var link: UnloadedVideoStreamRef
    var active: Boolean?
    var onLinkClicked: (UnloadedVideoStreamRef) -> Unit
}

internal val Link = fc<LinkProps> { (link, active, onLinkClicked) ->
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


private operator fun LinkProps.component1() = link
private operator fun LinkProps.component2() = active
private operator fun LinkProps.component3() = onLinkClicked
