package ui.show

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import react.RBuilder
import react.dom.div
import react.dom.onClick
import ui.*

class TvShowComponent(props: TvShowProps) : ViewModelComponent<TvShowProps, TvShowViewModel.State, TvShowViewModel>(props) {
    override fun getViewModel(): TvShowViewModel {
        return TvShowViewModelImpl(di.get(), di.get(), di.get(), di.get(), scope, props.tvShowKey)
    }

    override fun RBuilder.render() {
        val seasons = vmState.seasons
        if (seasons != null) {
            renderSeasons(seasons)
        } else {
            renderLoading()
        }
    }

    private fun RBuilder.renderSeasons(it: List<TulipSeasonInfo>) {
        div("list-group") {
            for (season in it) {
                renderSeason(season)
            }
        }
    }

    private fun RBuilder.renderSeason(season: TulipSeasonInfo) {
        activeListItem { +"Season ${season.key.seasonNumber}" }
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
}
