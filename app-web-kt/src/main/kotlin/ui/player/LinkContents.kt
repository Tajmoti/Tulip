package ui.player

import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import kotlinx.browser.window
import react.Props
import react.dom.*
import react.fc
import ui.shared.BadgeType
import ui.shared.LanguageBadge
import ui.shared.PillBadge

internal external interface LinkContentsProps : Props {
    var link: StreamingSiteLinkDto
}

internal val LinkContents = fc<LinkContentsProps> { (ref) ->
    val (serviceName, _, extractable, lang) = ref
    div("d-flex align-items-center justify-content-between ") {
        span {
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
        button(classes = "btn btn-secondary float-right") {
            attrs.onClick = { window.open(ref.url, "_blank") }
            i("fa-solid fa-up-right-from-square") {}
        }
    }
}


private operator fun LinkContentsProps.component1() = link
