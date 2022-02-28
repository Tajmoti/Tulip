package ui.react

import com.tajmoti.libtulip.ui.StateViewModel
import react.Props

abstract class ViewModelComponent<P : Props, S : Any, VM : StateViewModel<S>> : BaseComponent<P, ViewModelState<S>> {
    constructor() : super()
    constructor(props: P) : super(props)

    /**
     * ViewModel to use by this component. Available to subclasses.
     */
    protected val viewModel: VM by lazy { getViewModel() }

    /**
     * Instantiate the ViewModel to use for this component.
     */
    protected abstract fun getViewModel(): VM

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