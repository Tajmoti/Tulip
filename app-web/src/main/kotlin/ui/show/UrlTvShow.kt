package ui.show

import com.tajmoti.libtulip.model.hosted.StreamingService
import com.tajmoti.libtulip.model.key.*
import react.Props
import react.RBuilder
import react.fc
import react.router.dom.useSearchParams
import react.router.useNavigate
import react.router.useParams
import ui.player.VideoPlayerComponent
import ui.search.SearchComponent

val UrlTmdbTvShow = fc<Props> {
    val params = useParams()
    val key = TvShowKey.Tmdb(params["key"]!!.toLong())
    commonTvShow(key)
}

val UrlHostedTvShow = fc<Props> {
    val params = useParams()
    val service = StreamingService.valueOf(params["streamingService"]!!)
    val key = params["key"]!!
    commonTvShow(TvShowKey.Hosted(service, key))
}

private fun RBuilder.commonTvShow(key: TvShowKey) {
    val nav = useNavigate()
    child(TvShowComponent::class) {
        attrs.tvShowKey = key
        attrs.onEpisodeClicked = { nav(getUrlForStreamable(it)) }
    }
}

val UrlSearch = fc<Props> {
    val nav = useNavigate()
    val (params, _) = useSearchParams()

    child(SearchComponent::class) {
        attrs.query = params.get("query")!!
        attrs.onResultClicked = { nav(getUrlForItem(it)) }
    }
}

val UrlTmdbTvPlayer = fc<Props> {
    val params = useParams()
    val tvId = params["tvShowId"]!!.toLong()
    val seasonNumber = params["seasonNumber"]!!.toInt()
    val episodeNumber = params["episodeNumber"]!!.toInt()
    child(VideoPlayerComponent::class) {
        attrs.streamableKey = EpisodeKey.Tmdb(SeasonKey.Tmdb(TvShowKey.Tmdb(tvId), seasonNumber), episodeNumber)
    }
}

val UrlTmdbMoviePlayer = fc<Props> {
    val params = useParams()
    val movieId = params["movieId"]!!.toLong()
    child(VideoPlayerComponent::class) {
        attrs.streamableKey = MovieKey.Tmdb(movieId)
    }
}

val UrlHostedTvPlayer = fc<Props> {
    val params = useParams()
    val streamingService = StreamingService.valueOf(params["streamingService"]!!)
    val tvShowId = params[":tvShowId"]!!
    val seasonNumber = params["seasonNumber"]!!.toInt()
    val episodeId = params["episodeId"]!!
    val key = EpisodeKey.Hosted(SeasonKey.Hosted(TvShowKey.Hosted(streamingService, tvShowId), seasonNumber), episodeId)
    child(VideoPlayerComponent::class) {
        attrs.streamableKey = key
    }
}

val UrlHostedMoviePlayer = fc<Props> {
    val params = useParams()
    val streamingService = StreamingService.valueOf(params["streamingService"]!!)
    val movieId = params[":movieId"]!!
    val key = MovieKey.Hosted(streamingService, movieId)
    child(VideoPlayerComponent::class) {
        attrs.streamableKey = key
    }
}

private fun getUrlForItem(key: ItemKey): String {
    return when (key) {
        is TvShowKey.Tmdb -> "/tv/tmdb/${key.id}"
        is TvShowKey.Hosted -> "/tv/hosted/${key.streamingService}/${key.id}"
        is StreamableKey -> getUrlForStreamable(key)
    }
}

private fun getUrlForStreamable(key: StreamableKey): String {
    return when (key) {
        is EpisodeKey.Hosted -> "/player/tv/hosted/${key.streamingService}/${key.id}"
        is MovieKey.Hosted -> "/player/movie/hosted/${key.streamingService}/${key.id}"
        is EpisodeKey.Tmdb -> "/player/tv/tmdb/${key.tvShowKey.id}/${key.seasonNumber}/${key.episodeNumber}"
        is MovieKey.Tmdb -> "/player/movie/tmdb/${key.id}"
    }
}