package ui.common

import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.Props
import react.RBuilder
import react.dom.*
import ui.BaseComponent
import ui.library.LibraryComponent
import ui.player.VideoPlayerComponent
import ui.search.SearchComponent
import ui.show.TvShowComponent

class TulipComponent : BaseComponent<Props, TulipState>() {

    init {
        state.screen = TulipScreen.Library
    }

    override fun RBuilder.render() {
        renderNavbar()
        div("container py-2") {
            when (val scr = state.screen) {
                is TulipScreen.Library -> child(LibraryComponent::class) {

                }
                is TulipScreen.Search -> child(SearchComponent::class) {
                    attrs.query = scr.query
                    attrs.onResultClicked = { updateState { screen = getScreenForKey(it) } }
                }
                is TulipScreen.TvShow -> child(TvShowComponent::class) {
                    attrs.tvShowKey = scr.key
                    attrs.onEpisodeClicked = { updateState { screen = TulipScreen.Player(it) } }
                }
                is TulipScreen.Player -> child(VideoPlayerComponent::class) {
                    attrs.streamableKey = scr.key
                }
            }
        }
    }

    private fun getScreenForKey(key: ItemKey): TulipScreen {
        return when (key) {
            is TvShowKey -> TulipScreen.TvShow(key)
            is MovieKey -> TulipScreen.Player(key)
        }
    }

    private fun RBuilder.renderNavbar() {
        nav("navbar navbar-dark bg-dark") {
            a(classes = "navbar-brand text-light bg-dark") {
                +"Tulip"
            }
            form(classes = "form-inline") {
                input(type = InputType.search, classes = "form-control mr-sm-2") {
                    attrs.placeholder = "Search"
                    attrs.onChangeFunction = { event ->
                        val query = (event.target as HTMLInputElement).value
                        updateState { screen = TulipScreen.Search(query) }
                    }
                }
            }
        }
    }
}