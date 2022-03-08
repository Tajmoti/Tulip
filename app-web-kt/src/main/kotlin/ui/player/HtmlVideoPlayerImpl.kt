package ui.player

import com.tajmoti.libtulip.ui.player.MediaPlayerState
import com.tajmoti.libtulip.ui.player.Position
import com.tajmoti.libtulip.ui.streams.LoadedLink
import react.*
import react.dom.*

internal external interface HtmlVideoPlayerProps : Props {
    var link: LoadedLink
    var onStateChanged: (MediaPlayerState) -> Unit
    var initialProgress: Float?
}

internal val HtmlVideoPlayer = fc<HtmlVideoPlayerProps> { (link, onStateChanged, progress) ->
    val reference = useRef<dynamic>(null)
    val (progressRestored, setProgressRestored) = useState(false)
    video("w-100") {
        attrs.src = link.directLink
        attrs.onTimeUpdate = { onStateChanged(playerToState(reference, SimpleState.PLAYING)) }
        attrs.onPlay = { onStateChanged(playerToState(reference, SimpleState.PLAYING)) }
        attrs.onPause = { onStateChanged(playerToState(reference, SimpleState.PAUSED)) }
        attrs.onCanPlay = {
            if (!progressRestored) {
                reference.current.currentTime = (reference.current.duration * progress)
                setProgressRestored(true)
            }
        }
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
private operator fun HtmlVideoPlayerProps.component3() = initialProgress
