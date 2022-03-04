package ui.tvshow

import react.Props
import react.dom.h1
import react.fc

internal external interface TvShowDetailsProps : Props {
    var name: String
    var backdropUrl: String?
    var onFavoriteToggled: () -> Unit
    var isFavorite: Boolean?
}

internal val TvShowDetails = fc<TvShowDetailsProps> { props ->
    h1("mb-4") { +props.name }
}