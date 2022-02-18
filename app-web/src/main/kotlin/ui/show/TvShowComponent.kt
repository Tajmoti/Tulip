package ui.show

import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import react.RBuilder
import react.dom.div
import react.dom.onClick
import react.dom.span
import ui.TulipReactComponent
import ui.listButton
import ui.renderLoading

class TvShowComponent(props: TvShowProps) : TulipReactComponent<TvShowProps, TvShowState>(props) {
    private val viewModel = TvShowViewModelImpl(di.get(), di.get(), di.get(), di.get(), scope, props.tvShowKey)

    init {
        state.results = null
        viewModel.seasons flowTo { updateState { results = it } }
    }

    override fun RBuilder.render() {
        val seasons = state.results
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
        span("list-group-item active") { +"Season ${season.key.seasonNumber}" }
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
