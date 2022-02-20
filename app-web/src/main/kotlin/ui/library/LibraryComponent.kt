package ui.library

import com.tajmoti.libtulip.ui.library.LibraryViewModelImpl
import react.Props
import react.RBuilder
import react.State
import ui.BaseComponent

class LibraryComponent : BaseComponent<Props, State>() {
    private val viewModel = LibraryViewModelImpl(di.get(), di.get(), di.get(), di.get(), scope)

    override fun RBuilder.render() {

    }
}
