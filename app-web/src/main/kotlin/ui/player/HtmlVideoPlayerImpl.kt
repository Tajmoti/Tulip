package ui.player

import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.Position
import com.tajmoti.libtulip.ui.streams.LoadedLink
import react.*
import react.dom.onPause
import react.dom.onPlay
import react.dom.onTimeUpdate
import react.dom.video

internal external interface HtmlVideoPlayerProps : Props {
    var link: LoadedLink
    var onStateChanged: (MediaPlayerState) -> Unit
}

internal val HtmlVideoPlayer = fc<HtmlVideoPlayerProps> { (link, onMediaAttached) ->
    val reference = useRef<dynamic>(null)
    video("w-100") {
        attrs.src = link.directLink
        attrs.onTimeUpdate = { onMediaAttached(playerToState(reference, SimpleState.PLAYING)) }
        attrs.onPlay = { onMediaAttached(playerToState(reference, SimpleState.PLAYING)) }
        attrs.onPause = { onMediaAttached(playerToState(reference, SimpleState.PAUSED)) }
        attrs.controls = true
        attrs.autoPlay = true
        ref = reference
    }
}

private fun playerToState(ref: MutableRefObject<dynamic>, state: SimpleState): MediaPlayerState {
    val time = ((ref.current.currentTime as Float) * 1000).toLong()
    val length = ((ref.current.duration as Float) * 1000).toLong()
    val position = (time.toFloat() / length.toFloat())
    val posObj = Position(position, time)
    return when (state) {
        SimpleState.PLAYING -> MediaPlayerState.Playing(posObj, length)
        SimpleState.PAUSED -> MediaPlayerState.Paused(posObj, length)
        SimpleState.STOPPED -> MediaPlayerState.Idle
    }
}

enum class SimpleState {
    PLAYING,
    PAUSED,
    STOPPED
}


private operator fun HtmlVideoPlayerProps.component1() = link
private operator fun HtmlVideoPlayerProps.component2() = onStateChanged
