package ui

import AppDiHolder
import com.tajmoti.libtulip.ui.StateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import react.State
import react.useEffectOnce
import react.useState

inline fun <reified VM : StateViewModel<S>, S : Any> useViewModel(vararg parameters: Any?): Pair<VM, S>? {
    val (state, setState) = useState<ViewModelStateFc<VM, S>?>(null)
    useEffectOnce {
        val scope = CoroutineScope(Dispatchers.Default)
        val viewModel = AppDiHolder.di.get<VM> { parametersOf(scope, *parameters) }
        scope.launch { viewModel.state.collect { setState.invoke(wrapInModel(viewModel)) } }
        setState(wrapInModel(viewModel))
        cleanup { scope.cancel() }
    }
    return state?.let { it.viewModel to it.viewModel.state.value }
}

fun <VM : StateViewModel<S>, S : Any> wrapInModel(viewModel: VM): ViewModelStateFc<VM, S> {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
    val initial = object {} as ViewModelStateFc<VM, S>
    initial.viewModel = viewModel
    return initial
}

external interface ViewModelStateFc<VM : StateViewModel<S>, S : Any> : State {
    var viewModel: VM
}