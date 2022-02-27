package ui.common

import com.tajmoti.libtulip.TulipBuildInfo
import com.tajmoti.commonutils.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.Props
import react.createElement
import react.dom.*
import react.fc
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.useLocation
import react.router.useNavigate
import ui.*
import ui.library.Library

val Tulip = fc<Props> {
    HashRouter {
        TulipNavBar {}
        div("container py-2") {
            Routes {
                Route {
                    attrs.path = "/"
                    attrs.index = true
                    attrs.element = createElement(Library)
                }
                Route {
                    attrs.path = "/library"
                    attrs.element = createElement(Library)
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
                    attrs.element = createElement(UrlHostedTvPlayer)
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
    val location = useLocation()
    nav("navbar navbar-dark bg-dark") {
        a(classes = "navbar-brand text-light bg-dark") {
            attrs.onClickFunction = { nav.invoke("/") }
            +"Tulip"
        }
        form(classes = "form-inline") {
            input(type = InputType.search, classes = "form-control mr-sm-2") {
                attrs.placeholder = "Search"
                attrs.onChangeFunction = { event ->
                    val query = (event.target as HTMLInputElement).value
                    val dstUrl = "/search?query=${query}"
                    if (location.pathname == "/search") {
                        nav.invoke(dstUrl, jsObject { replace = true })
                    } else {
                        nav.invoke(dstUrl)
                    }
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