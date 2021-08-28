package com.tajmoti.tulip.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

fun RecyclerView.setupWithAdapterAndDivider(adapter: RecyclerView.Adapter<*>) {
    this.adapter = adapter
    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}

fun <T : RecyclerView.Adapter<*>> T.setToRecyclerWithDividers(rv: RecyclerView): T {
    rv.setupWithAdapterAndDivider(this)
    return this
}

suspend inline fun <T> runWithOnCancel(onCancel: T, crossinline block: suspend () -> T): T {
    return try {
        block()
    } catch (e: CancellationException) {
        onCancel
    }
}

fun <T> ViewModel.performStatefulOneshotOperation(
    state: MutableLiveData<T>,
    initial: T,
    onCancel: T,
    block: suspend () -> T
) {
    val currentClazz = state.value!!::class.java
    val initialClazz = state.value!!::class.java
    if (initialClazz != currentClazz)
        return
    state.value = initial!!
    viewModelScope.launch {
        state.value = runWithOnCancel(onCancel, block)!!
    }
}

fun Fragment.toast(@StringRes stringResId: Int) {
    Toast.makeText(requireContext(), stringResId, Toast.LENGTH_SHORT).show()
}