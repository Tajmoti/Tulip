package ui.library

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.seasonNumber
import com.tajmoti.libtulip.ui.library.LibraryItem
import react.Props
import react.dom.div
import react.dom.img
import react.dom.span
import react.fc
import react.router.dom.Link
import ui.getUrlForItem
import ui.getUrlForStreamable

external interface LibraryItemProps : Props {
    var item: LibraryItem
}

internal val LibraryItem = fc<LibraryItemProps> { props ->
    val item = props.item
    val (resume, label) = getEpisodeNumberLabelOrNull(item)
    div("m-2 bg-dark") {
        Link {
            attrs.to = getUrlForItem(item.key)
            div("row") {
                img(classes = "col", src = item.posterPath ?: "") {
                    attrs.width = "150em"
                    attrs.height = "225em"
                }
            }
        }
        if (resume) {
            Link {
                attrs.to = getUrlForStreamable(item.lastPlayedPosition!!.key)
                div("row p-2") {
                    span("col") { +label }
                }
            }
        } else {
            div("row p-2") {
                span("col") { +label }
            }
        }
    }
}

private fun getEpisodeNumberLabelOrNull(item: LibraryItem): Pair<Boolean, String> {
    return when (val key = item.lastPlayedPosition?.key) {
        is EpisodeKey.Hosted -> false to "Never watched"
        is EpisodeKey.Tmdb -> true to "▶ Resume S${key.seasonNumber}:E${key.episodeNumber}"
        is MovieKey.Hosted -> false to "Never watched"
        is MovieKey.Tmdb -> false to "Never watched"
        null -> false to "Never watched"
    }
}