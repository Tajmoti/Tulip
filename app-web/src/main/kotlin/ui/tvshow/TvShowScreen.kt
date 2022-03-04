package ui.tvshow

import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import react.Props
import react.fc
import ui.react.useViewModel
import ui.shared.LoadingSpinner
import ui.shared.SpinnerColor

external interface TvShowScreenProps : Props {
    var tvShowKey: TvShowKey
}

val TvShowScreen = fc<TvShowScreenProps> { props ->
    val (vm, vmState) = useViewModel<TvShowViewModel, TvShowViewModel.State>(props.tvShowKey) ?: return@fc

    vmState.name?.let { name ->
        TvShowDetails {
            attrs.name = name
            attrs.backdropUrl = vmState.backdropPath
            attrs.onFavoriteToggled = vm::toggleFavorites
            attrs.isFavorite = vmState.isFavorite
        }
    }

    val seasons = vmState.seasons
    if (seasons != null) {
        val currentSeason = getCurrentSeason(seasons, vmState.selectedSeason)
        TvShow {
            attrs.seasons = seasons
            attrs.currentSeason = currentSeason
            attrs.onSeasonSelected = vm::onSeasonSelected
            attrs.isFavorite = vm.isFavorite.value
            attrs.onFavoriteToggled = vm::toggleFavorites
        }
    } else {
        LoadingSpinner { attrs.color = SpinnerColor.Default }
    }
}

private fun getCurrentSeason(seasons: List<TulipSeasonInfo>, preselected: SeasonKey?): TulipSeasonInfo {
    val currentSeasonKey = preselected ?: seasons.first().key
    return seasons.first { it.key == currentSeasonKey }
}
