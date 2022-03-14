@file:Suppress("unused")

package com.tajmoti.tulip.ui.utils

import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tajmoti.commonutils.LibraryDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

fun RecyclerView.setupWithAdapterAndDivider(adapter: RecyclerView.Adapter<*>) {
    this.adapter = adapter
    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}

fun <T : RecyclerView.Adapter<*>> T.setToRecyclerWithDividers(rv: RecyclerView): T {
    rv.setupWithAdapterAndDivider(this)
    return this
}

fun Fragment.toast(@StringRes stringResId: Int) {
    Toast.makeText(requireContext(), stringResId, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(@StringRes stringResId: Int) {
    Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

inline val AppCompatActivity.isInPipModeCompat: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode

fun <T> Fragment.consume(flow: Flow<T>, collector: FlowCollector<T>) {
    viewLifecycleOwner.consume(flow, collector)
}

fun <T : Any> Fragment.consumeNotNull(flow: Flow<T?>, collector: FlowCollector<T>) {
    viewLifecycleOwner.consume(flow) { it?.let { collector.emit(it) } }
}

fun <T> LifecycleOwner.consume(flow: Flow<T>, collector: FlowCollector<T>) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collector)
        }
    }
}

val ViewModel.delegatingViewModelScope: CoroutineScope
    get() = viewModelScope + LibraryDispatchers.libraryContext