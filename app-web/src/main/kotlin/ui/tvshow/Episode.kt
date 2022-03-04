package ui.tvshow

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import react.Props
import react.dom.*
import react.fc
import react.router.useNavigate
import ui.getUrlForStreamable
import ui.shared.ListButton

internal external interface EpisodeProps : Props {
    var episode: TulipEpisodeInfo
}

internal val Episode = fc<EpisodeProps> { props ->
    val nav = useNavigate()
    ListButton {
        attrs.contents = {
            with(it) {
                div("d-flex flex-row") {
                    img(src = props.episode.stillPath ?: "", classes = "img-letterbox flex-shrink-0") {
                        attrs.width = "160em"
                        attrs.height = "90em"
                    }
                    div("ml-2") {
                        h5 { +"${props.episode.episodeNumber}. ${props.episode.name}" }
                        span("text-3-lines") { +(props.episode.overview ?: "Overview unavailable") }
                    }
                }
                attrs.onClick = { _ -> nav(getUrlForStreamable(props.episode.key)) }
            }
        }
    }
}