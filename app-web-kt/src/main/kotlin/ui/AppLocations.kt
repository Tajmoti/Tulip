package ui

import com.tajmoti.libtulip.model.key.*
import react.Props
import react.fc
import react.router.dom.useSearchParams
import react.router.useNavigate
import react.router.useParams
import ui.player.VideoPlayerScreen
import ui.search.SearchScreen
import ui.tvshow.TvShowScreen


val UrlTmdbTvShow = fc<Props> {
    val params = useParams()
    TvShowScreen { attrs.tvShowKey = TvShowKey.Tmdb(params["key"]!!.toLong()) }
}

val UrlHostedTvShow = fc<Props> {
    val params = useParams()
    val service = params["streamingService"]!!
    val key = params["key"]!!
    TvShowScreen { attrs.tvShowKey = TvShowKey.Hosted(service, key) }
}

val UrlSearch = fc<Props> {
    val nav = useNavigate()
    val (params, _) = useSearchParams()

    SearchScreen {
        attrs.query = params.get("query")!!
        attrs.onResultClicked = { nav(getUrlForItem(it)) }
    }
}

val UrlTmdbTvPlayer = fc<Props> {
    val params = useParams()
    val tvId = params["tvShowId"]!!.toLong()
    val seasonNumber = params["seasonNumber"]!!.toInt()
    val episodeNumber = params["episodeNumber"]!!.toInt()
    VideoPlayerScreen {
        attrs.streamableKey = EpisodeKey.Tmdb(SeasonKey.Tmdb(TvShowKey.Tmdb(tvId), seasonNumber), episodeNumber)
    }
}

val UrlTmdbMoviePlayer = fc<Props> {
    val params = useParams()
    val movieId = params["movieId"]!!.toLong()
    VideoPlayerScreen {
        attrs.streamableKey = MovieKey.Tmdb(movieId)
    }
}

val UrlHostedTvPlayer = fc<Props> {
    val params = useParams()
    val streamingService = params["streamingService"]!!
    val tvShowId = params[":tvShowId"]!!
    val seasonNumber = params["seasonNumber"]!!.toInt()
    val episodeId = params["episodeId"]!!
    val key = EpisodeKey.Hosted(SeasonKey.Hosted(TvShowKey.Hosted(streamingService, tvShowId), seasonNumber), episodeId)
    VideoPlayerScreen {
        attrs.streamableKey = key
    }
}

val UrlHostedMoviePlayer = fc<Props> {
    val params = useParams()
    val streamingService = params["streamingService"]!!
    val movieId = params[":movieId"]!!
    val key = MovieKey.Hosted(streamingService, movieId)
    VideoPlayerScreen {
        attrs.streamableKey = key
    }
}


fun getUrlForItem(key: ItemKey): String {
    return when (key) {
        is TvShowKey.Tmdb -> "/tv/tmdb/${key.id}"
        is TvShowKey.Hosted -> "/tv/hosted/${key.streamingService}/${key.id}"
        is StreamableKey -> getUrlForStreamable(key)
    }
}

fun getUrlForStreamable(key: StreamableKey): String {
    return when (key) {
        is EpisodeKey.Hosted -> "/player/tv/hosted/${key.streamingService}/${key.id}"
        is MovieKey.Hosted -> "/player/movie/hosted/${key.streamingService}/${key.id}"
        is EpisodeKey.Tmdb -> "/player/tv/tmdb/${key.tvShowKey.id}/${key.seasonNumber}/${key.episodeNumber}"
        is MovieKey.Tmdb -> "/player/movie/tmdb/${key.id}"
    }
}