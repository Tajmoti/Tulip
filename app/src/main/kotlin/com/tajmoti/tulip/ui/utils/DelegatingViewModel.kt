package com.tajmoti.tulip.ui.utils

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

abstract class DelegatingViewModel<T> : ViewModel() {
    abstract val impl: T
}

@MainThread
inline fun <T, reified VM : DelegatingViewModel<T>> Fragment.activityViewModelsDelegated(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = activityViewModels<VM>(factoryProducer).map { it.impl }

@MainThread
inline fun <T, reified VM : DelegatingViewModel<T>> Fragment.viewModelsDelegated(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = viewModels<VM>(ownerProducer, factoryProducer).map { it.impl }

@MainThread
inline fun <T, reified VM : DelegatingViewModel<T>> ComponentActivity.viewModelsDelegated(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = viewModels<VM>(factoryProducer).map { it.impl }

fun <T, S> Lazy<T>.map(mapper: (T) -> S): Lazy<S> {
    return object : Lazy<S> {
        override val value: S
            get() = mapper(this@map.value)

        override fun isInitialized(): Boolean {
            return this@map.isInitialized()
        }
    }
}