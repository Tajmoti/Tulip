package ui.common

import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey

sealed class TulipScreen {
    object Library : TulipScreen()
    data class Search(val query: String) : TulipScreen()
    data class TvShow(val key: TvShowKey) : TulipScreen()
    data class Player(val key: StreamableKey) : TulipScreen()
}