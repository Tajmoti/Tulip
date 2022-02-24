package ui.common

import com.tajmoti.libtulip.TulipBuildInfo
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.Props
import react.createElement
import react.dom.*
import react.fc
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.useNavigate
import ui.library.LibraryComponent
import ui.show.*

val Tulip = fc<Props> {
    HashRouter {
        TulipNavBar {}
        div("container py-2") {
            Routes {
                Route {
                    attrs.path = "/"
                    attrs.index = true
                    attrs.element = createElement { LibraryComponent() }
                }
                Route {
                    attrs.path = "/library"
                    attrs.element = createElement { LibraryComponent() }
                }
                Route {
                    attrs.path = "/search"
                    attrs.element = createElement(UrlSearch)
                }
                Route {
                    attrs.path = "/tv/tmdb/:key"
                    attrs.element = createElement(UrlTmdbTvShow)
                }
                Route {
                    attrs.path = "/tv/hosted/:streamingService/:key"
                    attrs.element = createElement(UrlHostedTvShow)
                }
                Route {
                    attrs.path = "/player/tv/tmdb/:tvShowId/:seasonNumber/:episodeNumber"
                    attrs.element = createElement(UrlTmdbTvPlayer)
                }
                Route {
                    attrs.path = "/player/tv/hosted/:streamingService/:tvShowId/:seasonNumber/:episodeId"
                    attrs.element = createElement(UrlHostedTvPlayer)
                }
                Route {
                    attrs.path = "/player/movie/tmdb/:movieId"
                    attrs.element = createElement(UrlTmdbMoviePlayer)
                }
                Route {
                    attrs.path = "/player/movie/hosted/:streamingService/:movieId"
                    attrs.element = createElement(UrlHostedMoviePlayer)
                }
            }
        }
        TulipFooter {}
    }
}

private val TulipNavBar = fc<Props> {
    val nav = useNavigate()
    nav("navbar navbar-dark bg-dark") {
        a(classes = "navbar-brand text-light bg-dark") {
            +"Tulip"
        }
        form(classes = "form-inline") {
            input(type = InputType.search, classes = "form-control mr-sm-2") {
                attrs.placeholder = "Search"
                attrs.onChangeFunction = { event ->
                    val query = (event.target as HTMLInputElement).value
                    nav.invoke("/search?query=${query}")
                }
            }
        }
    }
}

private val TulipFooter = fc<Props> {
    footer("navbar navbar-dark bg-dark text-light mt-auto mt-2") {
        span("navbar-text") {
            +"Build ${TulipBuildInfo.commit}"
        }
    }
}