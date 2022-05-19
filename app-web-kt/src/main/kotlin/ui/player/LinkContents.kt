package ui.player

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import react.Props
import react.fc
import ui.shared.BadgeType
import ui.shared.LanguageBadge
import ui.shared.PillBadge

internal external interface LinkContentsProps : Props {
    var link: StreamingSiteLinkDto
}

internal val LinkContents = fc<LinkContentsProps> { (ref) ->
    val (serviceName, _, extractable, lang) = ref
    +serviceName
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
    LanguageBadge { attrs.language = lang }
}


private operator fun LinkContentsProps.component1() = link
