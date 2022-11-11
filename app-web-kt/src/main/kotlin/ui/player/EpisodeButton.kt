package ui.player

import com.tajmoti.libtulip.model.key.EpisodeKey
import react.Props
import react.RBuilder
import react.dom.button
import react.dom.i
import react.dom.span
import react.fc
import react.router.dom.Link
import ui.getUrlForStreamable
import ui.player.EpisodeButtonDirection.Backward
import ui.player.EpisodeButtonDirection.Forward

internal external interface EpisodeButtonProps : Props {
    var label: String
    var key: EpisodeKey?
    var direction: EpisodeButtonDirection
}

internal val EpisodeButton = fc<EpisodeButtonProps> { (label, key, direction) ->
    renderEpisodeButton(key, label, direction)
}

enum class EpisodeButtonDirection {
    Backward,
    Forward,
}

// TODO figure out why EpisodeButton is rendering twice and remove
fun RBuilder.renderEpisodeButton(key: EpisodeKey?, label: String, direction: EpisodeButtonDirection) {
    Link {
        val (iconClass, directionClass) = when (direction) {
            Backward -> "mr-2 fa-backward-step" to ""
            Forward -> "ml-2 fa-forward-step" to "flex-row-reverse"
        }
        button(classes = "btn btn-primary d-flex align-items-center $directionClass") {
            attrs.disabled = key == null
            i("fa-solid $iconClass") {}
            span { +label }
        }
        attrs.to = key?.let { getUrlForStreamable(it) } ?: ""
    }
}


private operator fun EpisodeButtonProps.component1() = label
private operator fun EpisodeButtonProps.component2() = key
private operator fun EpisodeButtonProps.component3() = direction
