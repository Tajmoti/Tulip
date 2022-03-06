package ui

import AppDiHolder
import com.tajmoti.commonutils.jsObject
import com.tajmoti.libtulip.ui.StateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import react.State
import react.useEffectOnce
import react.useState

inline fun <reified VM : StateViewModel<S>, S : Any> useViewModel(vararg parameters: Any?): Pair<VM, S> {
    val (scope, _) = useState { CoroutineScope(Dispatchers.Default) }
    val (vm, _) = useState { AppDiHolder.di.get<VM> { parametersOf(scope, *parameters) } }
    val (state, setState) = useState { wrapInModel(vm) }
    useEffectOnce {
        scope.launch { vm.state.collect { setState.invoke(wrapInModel(vm)) } }
        cleanup { scope.cancel() }
    }
    return state.let { it.viewModel to it.viewModel.state.value }
}

fun <VM : StateViewModel<S>, S : Any> wrapInModel(viewModel: VM): ViewModelStateFc<VM, S> {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
    return jsObject { this.viewModel = viewModel }
}

external interface ViewModelStateFc<VM : StateViewModel<S>, S : Any> : State {
    var viewModel: VM
}