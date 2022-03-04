package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import react.Props
import react.fc
import ui.BadgeType
import ui.PillBadge
import ui.renderLanguageBadge

internal external interface LinkContentsProps : Props {
    var link: UnloadedVideoStreamRef
}

internal val LinkContents = fc<LinkContentsProps> { (ref) ->
    val (info, extractable, lang) = ref
    +info.serviceName
    if (extractable) {
        PillBadge {
            attrs.color = BadgeType.Success
            attrs.message = "Playable"
        }
    } else {
        PillBadge {
            attrs.color = BadgeType.Danger
            attrs.message = "Playable externally"
        }
    }
    renderLanguageBadge(lang)
}


private operator fun LinkContentsProps.component1() = link
