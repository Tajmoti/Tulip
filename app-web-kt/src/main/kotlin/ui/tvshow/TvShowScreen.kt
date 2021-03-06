package ui.tvshow

import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import react.Props
import react.fc
import ui.shared.LoadingSpinner
import ui.shared.SpinnerColor
import ui.useViewModel

external interface TvShowScreenProps : Props {
    var tvShowKey: TvShowKey
}

val TvShowScreen = fc<TvShowScreenProps> { props ->
    val (vm, vmState) = useViewModel<TvShowViewModel, TvShowViewModel.State>(props.tvShowKey)

    vmState.name?.let { name ->
        TvShowDetails {
            attrs.name = name
            attrs.backdropUrl = vmState.backdropPath
            attrs.onFavoriteToggled = vm::toggleFavorites
            attrs.isFavorite = vmState.isFavorite
        }
    }

    val seasons = vmState.seasons
    val currentSeason = vmState.selectedSeason
    if (seasons != null && currentSeason != null) {
        TvShow {
            attrs.seasons = seasons
            attrs.currentSeason = currentSeason
            attrs.lastPlayedEpisode = vmState.lastPlayedEpisode
            attrs.onSeasonSelected = vm::onSeasonSelected
            attrs.isFavorite = vm.isFavorite.value
            attrs.onFavoriteToggled = vm::toggleFavorites
        }
    } else {
        LoadingSpinner { attrs.color = SpinnerColor.Default }
    }
}
