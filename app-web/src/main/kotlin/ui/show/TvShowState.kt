package ui.show

import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import react.State

external interface TvShowState : State {
    var results: List<TulipSeasonInfo>?
}