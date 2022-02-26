package ui.show

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.seasonNumber
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import org.w3c.dom.HTMLSelectElement
import react.RBuilder
import react.dom.*
import ui.ViewModelComponent
import ui.listButton
import ui.renderLoading

class TvShowComponent(props: TvShowProps) : ViewModelComponent<TvShowProps, TvShowViewModel.State, TvShowViewModel>(props) {
    override fun getViewModel(): TvShowViewModel {
        return TvShowViewModelImpl(di.get(), di.get(), di.get(), di.get(), scope, props.tvShowKey)
    }

    override fun RBuilder.render() {
        val seasons = vmState.seasons
        vmState.name?.let { name -> renderName(name) }
        if (seasons != null) {
            val currentSeason = getCurrentSeason(seasons, vmState.selectedSeason)
            renderSeasonDropdown(seasons, currentSeason)
            renderSeason(currentSeason)
        } else {
            renderLoading()
        }
    }

    private fun RBuilder.renderName(name: String) {
        h1("mb-4") { +name }
    }

    private fun getCurrentSeason(seasons: List<TulipSeasonInfo>, preselected: SeasonKey?): TulipSeasonInfo {
        val currentSeasonKey = preselected ?: seasons.first().key
        return seasons.first { it.key == currentSeasonKey }
    }

    private fun RBuilder.renderSeason(season: TulipSeasonInfo) {
        for (episode in season.episodes) {
            renderEpisode(episode)
        }
    }

    private fun RBuilder.renderEpisode(episode: TulipEpisodeInfo) {
        listButton {
            +"${episode.episodeNumber} ${episode.name}"
            attrs.onClick = { _ -> props.onEpisodeClicked(episode.key) }
        }
    }

    private fun RBuilder.renderSeasonDropdown(seasons: List<TulipSeasonInfo>, selected: TulipSeasonInfo) {
        select("custom-select mb-2") {
            for (season in seasons) {
                renderSeasonDropdownItem(season)
            }
            attrs.value = selected.seasonNumber.toString()
            attrs.onChange = {
                val seasonNumber = (it.target as HTMLSelectElement).value.toInt()
                onSeasonNumberSelected(seasons, seasonNumber)
            }
        }
    }

    private fun onSeasonNumberSelected(seasons: List<TulipSeasonInfo>, seasonNumber: Int) {
        val season = seasons.first { season -> season.seasonNumber == seasonNumber }
        viewModel.onSeasonSelected(season.key)
    }

    private fun RBuilder.renderSeasonDropdownItem(season: TulipSeasonInfo) {
        option {
            +"Season ${season.seasonNumber}"
            attrs.value = season.seasonNumber.toString()
        }
    }
}
