package ui.player

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import kotlinx.html.CommonAttributeGroupFacade
import react.RBuilder
import react.dom.RDOMBuilder
import react.dom.div
import react.dom.onClick
import ui.TulipReactComponent
import ui.activeListItem
import ui.listButton
import ui.renderLanguageBadge

class LinkListComponent(props: LinkListProps) : TulipReactComponent<LinkListProps, LinkListState>(props) {

    init {
        state.items = emptyList()
        state.playingLink = null
        props.viewModel.linksResult flowValTo { updateState { items = it } }
        props.viewModel.videoLinkToPlay flowValTo { updateState { playingLink = it.stream.origin ?: it.stream } }
    }

    override fun RBuilder.render() {
        div("list-group") {
            for (ref in state.items) {
                renderLink(ref)
            }
        }
    }

    private fun RBuilder.renderLink(ref: UnloadedVideoStreamRef) {
        if (state.playingLink == ref.info) {
            activeListItem { fillBadge(ref) }
        } else {
            listButton { fillBadge(ref) }
        }
    }

    private fun RDOMBuilder<CommonAttributeGroupFacade>.fillBadge(ref: UnloadedVideoStreamRef) {
        val (info, extractable, lang) = ref
        val watchable = if (extractable) "Watchable" else "Not yet watchable"
        +"${info.serviceName} $watchable"
        renderLanguageBadge(lang, extraClasses = "ml-1")
        attrs.onClick = { props.itemCallback(ref) }
    }
}