package ui.application

import react.Props
import react.createElement
import react.dom.div
import react.fc
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import ui.*
import ui.library.LibraryScreen

val Application = fc<Props> {
    HashRouter {
        NavBar {}
        div("container py-2") {
            Routes {
                Route {
                    attrs.path = "/"
                    attrs.index = true
                    attrs.element = createElement(LibraryScreen)
                }
                Route {
                    attrs.path = "/library"
                    attrs.element = createElement(LibraryScreen)
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
        Footer {}
    }
}
