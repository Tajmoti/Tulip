package ui.library

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.seasonNumber
import com.tajmoti.libtulip.dto.LibraryItemDto
import react.Props
import react.dom.div
import react.dom.img
import react.dom.p
import react.fc
import react.router.dom.Link
import ui.getUrlForItem
import ui.getUrlForStreamable

external interface LibraryItemProps : Props {
    var item: LibraryItemDto
}

internal val LibraryItem = fc<LibraryItemProps> { props ->
    val item = props.item
    val (resume, label) = getEpisodeNumberLabelOrNull(item)
    div("m-2 bg-dark") {
        Link {
            attrs.to = getUrlForItem(item.key)
            img(src = item.posterPath ?: "") {
                attrs.width = "150em"
                attrs.height = "225em"
            }
        }
        if (resume) {
            Link {
                attrs.to = getUrlForStreamable(item.playingProgress!!.key)
                p("m-0 p-2") { +label }
            }
        } else {
            p("m-0 p-2") { +label }
        }
    }
}

private fun getEpisodeNumberLabelOrNull(item: LibraryItemDto): Pair<Boolean, String> {
    return when (val key = item.playingProgress?.key) {
        is EpisodeKey.Hosted -> false to "Never watched"
        is EpisodeKey.Tmdb -> true to "▶ Resume S${key.seasonNumber}:E${key.episodeNumber}"
        is MovieKey.Hosted -> false to "Never watched"
        is MovieKey.Tmdb -> false to "Never watched"
        null -> false to "Never watched"
    }
}