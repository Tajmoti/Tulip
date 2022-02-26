package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtvprovider.model.VideoStreamRef
import kotlinx.html.CommonAttributeGroupFacade
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.onClick
import ui.*

class LinkListComponent(props: LinkListProps) : ViewModelComponent<LinkListProps, VideoPlayerViewModel.State, VideoPlayerViewModel>(props) {
    override fun getViewModel() = props.viewModel

    override fun RBuilder.render() {
        div("list-group") {
            for (ref in vmState.linkListState.linksResult ?: emptyList()) {
                renderLink(ref)
            }
        }
    }

    private fun RBuilder.renderLink(ref: UnloadedVideoStreamRef) {
        if (vmState.selectedLinkState.videoLinkPreparingOrPlaying?.getInitiallySelectedLink() == ref.info) {
            activeListItem { fillBadge(ref) }
        } else {
            listButton { fillBadge(ref) }
        }
    }

    private fun RDOMBuilder<CommonAttributeGroupFacade>.fillBadge(ref: UnloadedVideoStreamRef) {
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
        attrs.onClick = { props.itemCallback(ref) }
    }

    private fun VideoStreamRef.getInitiallySelectedLink(): VideoStreamRef {
        return when (this) {
            is VideoStreamRef.Unresolved -> this
            is VideoStreamRef.Resolved -> origin ?: this
        }
    }
}