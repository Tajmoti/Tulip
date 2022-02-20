package ui

import com.tajmoti.libtulip.ui.StateViewModel
import react.Props

abstract class ViewModelComponent<P : Props, S : Any>(
    viewModel: StateViewModel<S>,
    props: P
) : BaseComponent<P, ViewModelState<S>>(props) {

    /**
     * State of the ViewModel.
     */
    protected val vmState: S
        get() = state.state


    init {
        state.state = viewModel.state.value
        viewModel.state flowTo { updateState { state = it } }
    }
}