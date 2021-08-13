package com.tajmoti.tulip.ui

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


suspend fun <T> runWithOnCancel(onCancel: T, block: suspend () -> T): T {
    return try {
        block()
    } catch (e: CancellationException) {
        onCancel
    }
}

fun <T> ViewModel.performStatefulOneshotOperation(
    state: MutableLiveData<T>,
    initial: T?,
    onCancel: T?,
    block: suspend () -> T
) {
    val currentClazz = state.value?.let { it::class.java }
    val initialClazz = state.value?.let { it::class.java }
    if (initialClazz != currentClazz)
        return
    state.value = initial
    viewModelScope.launch {
        state.value = runWithOnCancel(onCancel, block)
    }
}