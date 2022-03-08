package ui.player

import com.tajmoti.libtulip.model.key.EpisodeKey
import react.Props
import react.RBuilder
import react.dom.button
import react.fc
import react.router.dom.Link
import ui.getUrlForStreamable

internal external interface EpisodeButtonProps : Props {
    var label: String
    var key: EpisodeKey?
}

internal val EpisodeButton = fc<EpisodeButtonProps> { (label, key) ->
    renderEpisodeButton(key, label)
}

// TODO figure out why EpisodeButton is rendering twice and remove
fun RBuilder.renderEpisodeButton(key: EpisodeKey?, label: String) {
    Link {
        button(classes = "btn btn-primary") {
            attrs.disabled = key == null
            +label
        }
        attrs.to = key?.let { getUrlForStreamable(it) } ?: ""
    }
}


private operator fun EpisodeButtonProps.component1() = label
private operator fun EpisodeButtonProps.component2() = key
